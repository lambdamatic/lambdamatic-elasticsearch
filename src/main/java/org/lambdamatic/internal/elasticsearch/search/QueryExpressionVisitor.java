
package org.lambdamatic.internal.elasticsearch.search;

import java.lang.reflect.Method;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.common.geo.builders.ShapeBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.BoostableQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.lambdamatic.analyzer.ast.node.CompoundExpression;
import org.lambdamatic.analyzer.ast.node.Expression;
import org.lambdamatic.analyzer.ast.node.Expression.ExpressionType;
import org.lambdamatic.analyzer.ast.node.ExpressionVisitor;
import org.lambdamatic.analyzer.ast.node.FieldAccess;
import org.lambdamatic.analyzer.ast.node.MethodInvocation;
import org.lambdamatic.elasticsearch.annotations.DocumentField;
import org.lambdamatic.elasticsearch.exceptions.ConversionException;
import org.lambdamatic.elasticsearch.querydsl.QueryExpression;
import org.lambdamatic.elasticsearch.types.Location;
import org.lambdamatic.internal.elasticsearch.QueryClauseType;
import org.lambdamatic.internal.elasticsearch.QueryClauseType.EnumQueryClauseType;
import org.lambdamatic.internal.elasticsearch.QueryOperator;
import org.lambdamatic.internal.elasticsearch.QueryOperator.QueryOperatorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link ExpressionVisitor} for a {@link QueryExpression} that will prepare the
 * {@link SearchRequest}.
 */
public class QueryExpressionVisitor extends ExpressionVisitor {

  /** The usual Logger. */
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(QueryExpressionVisitor.class);

  private QueryBuilder queryBuilder;

  @Override
  public boolean visitCompoundExpression(final CompoundExpression expr) {
    final BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
    switch (expr.getOperator()) {
      case CONDITIONAL_AND:
        break;
      case CONDITIONAL_OR:
        break;
      default:
        break;
    }
    for (Expression operand : expr.getOperands()) {
      final QueryExpressionVisitor operandVisitor = new QueryExpressionVisitor();
      operand.accept(operandVisitor);
      boolQueryBuilder.must(operandVisitor.getQueryBuilder());
    }
    return false;
  }

  @Override
  public boolean visitMethodInvocationExpression(final MethodInvocation methodInvocation) {
    final QueryClauseType searchOperation =
        methodInvocation.getJavaMethod().getAnnotation(QueryClauseType.class);
    // when source type is a 'field access', it's the root/start of the query builder.
    if (methodInvocation.getSource().getExpressionType() == ExpressionType.FIELD_ACCESS) {
      final FieldAccess field = (FieldAccess) methodInvocation.getSource();
      final String fieldName = getDocumentFieldName(field);
      final Expression value = methodInvocation.getArguments().get(0);
      final EnumQueryClauseType searchType = searchOperation.value();
      switch (searchType) {
        case FUZZY:
          this.queryBuilder = QueryBuilders.fuzzyQuery(fieldName, value);
          break;
        case MATCHES:
          this.queryBuilder = QueryBuilders.matchQuery(fieldName, value);
          break;
        case GEO_WITHIN_RECTANGLE:
          final Location topLeft = (Location) methodInvocation.getArguments().get(0).getValue();
          final Location bottomRight = (Location) methodInvocation.getArguments().get(1).getValue();
          this.queryBuilder = QueryBuilders.geoBoundingBoxQuery(fieldName)
              .topLeft(topLeft.getLatitude(), topLeft.getLongitude())
              .bottomRight(bottomRight.getLatitude(), bottomRight.getLongitude());
          break;
        default:
          throw new ConversionException("Unsupported search type: " + searchType);
      }
    } else if (methodInvocation.getSource().getExpressionType() == ExpressionType.METHOD_INVOCATION
        && this.queryBuilder != null) {
      // chain other operation on the query builder
      final Method javaMethod = methodInvocation.getJavaMethod();
      final QueryOperator queryOperator = javaMethod.getAnnotation(QueryOperator.class);
      if (queryOperator != null && queryOperator.value() == QueryOperatorType.BOOST) {
        final float boostFactor = (float) methodInvocation.getArguments().get(0).getValue();
        ((BoostableQueryBuilder<?>) this.queryBuilder).boost(boostFactor);
      }

    } else {
      throw new ConversionException("Unsupported expression type: '"
          + methodInvocation.getSource().getExpressionType().name() + "' or invalid state.");
    }
    return false;
  }

  /**
   * Retrieves the {@link DocumentField#name()} from the given {@link FieldAccess} in a
   * {@link QueryExpression}. If the field is nested into another domain object, the full path is
   * retrieved.
   * 
   * @param field the {@link FieldAccess} in the {@link QueryExpression}
   * @return the name of the field in the Elasticsearch/Lucene document.
   */
  private static String getDocumentFieldName(final FieldAccess field) {
    final Class<?> sourceType = field.getSource().getJavaType();
    try {
      final String fieldName =
          sourceType.getField(field.getFieldName()).getAnnotation(DocumentField.class).name();
      if (field.getSource().getExpressionType() == ExpressionType.FIELD_ACCESS) {
        return getDocumentFieldName((FieldAccess) field.getSource()) + "." + fieldName;
      }
      if (fieldName == null || fieldName.isEmpty()) {
        // assume that the Elasticsearch document field has the same name as the domain field
        return field.getFieldName();
      }
      return fieldName;
    } catch (NoSuchFieldException e) {
      throw new ConversionException("Failed to retrieve field named '" + field.getFieldName()
          + "' on type '" + sourceType.getName() + "'", e);
    }
  }

  /**
   * @return the {@link QueryBuilder} corresponding to the {@link QueryExpression} that was visited.
   */
  public QueryBuilder getQueryBuilder() {
    return this.queryBuilder;
  }

}
