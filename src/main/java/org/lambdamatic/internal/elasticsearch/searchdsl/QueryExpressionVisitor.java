
package org.lambdamatic.internal.elasticsearch.searchdsl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.lambdamatic.analyzer.ast.node.CompoundExpression;
import org.lambdamatic.analyzer.ast.node.Expression.ExpressionType;
import org.lambdamatic.analyzer.ast.node.ExpressionVisitor;
import org.lambdamatic.analyzer.ast.node.FieldAccess;
import org.lambdamatic.analyzer.ast.node.MethodInvocation;
import org.lambdamatic.elasticsearch.annotations.DocumentField;
import org.lambdamatic.elasticsearch.annotations.FullText;
import org.lambdamatic.elasticsearch.annotations.Keyword;
import org.lambdamatic.elasticsearch.exceptions.CodecException;
import org.lambdamatic.elasticsearch.exceptions.ConversionException;
import org.lambdamatic.elasticsearch.searchdsl.QueryExpression;
import org.lambdamatic.elasticsearch.searchdsl.types.QueryClauseType;
import org.lambdamatic.elasticsearch.searchdsl.types.QueryClauseType.EnumQueryClauseType;
import org.lambdamatic.elasticsearch.searchdsl.types.QueryOperator;
import org.lambdamatic.elasticsearch.searchdsl.types.QueryOperator.QueryOperatorType;
import org.lambdamatic.elasticsearch.types.Location;
import org.lambdamatic.internal.elasticsearch.searchdsl.RangeQuery.RangeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link ExpressionVisitor} for a {@link QueryExpression} that will prepare the
 * {@link DocumentQueryDelegate}.
 */
class QueryExpressionVisitor extends ExpressionVisitor {

  /** The usual Logger. */
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(QueryExpressionVisitor.class);

  private Query query;

  /**
   * @return the {@link Query} corresponding to the {@link QueryExpression} that was visited.
   */
  public Query getQuery() {
    return this.query;
  }

  @Override
  public boolean visitCompoundExpression(final CompoundExpression expr) {
    final List<Query> operandQueries = expr.getOperands().stream().map(operand -> {
      final QueryExpressionVisitor operandVisitor = new QueryExpressionVisitor();
      operand.accept(operandVisitor);
      return operandVisitor.getQuery();
    }).collect(Collectors.toList());
    switch (expr.getOperator()) {
      case CONDITIONAL_AND:
        this.query = BooleanQuery.and(operandQueries);
        break;
      case CONDITIONAL_OR:
        this.query = BooleanQuery.or(operandQueries);
        break;
      default:
        break;
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
      final EnumQueryClauseType searchType = searchOperation.value();
      switch (searchType) {
        case TERM:
          this.query = new TermQuery(fieldName, methodInvocation.getArgumentValue(0));
          break;
        case MATCHES:
          this.query = new MatchQuery(fieldName, methodInvocation.getArgumentValue(0));
          break;
        case RANGE_GT:
          this.query = new RangeQuery(RangeType.GT, fieldName, methodInvocation.getArgumentValue(0));
          break;
        case RANGE_GTE:
          this.query = new RangeQuery(RangeType.GTE, fieldName, methodInvocation.getArgumentValue(0));
          break;
        case GEO_WITHIN_RECTANGLE:
          final Location topLeft = (Location) methodInvocation.getArgumentValue(0);
          final Location bottomRight = (Location) methodInvocation.getArgumentValue(1);
          this.query = GeoBoundingBoxQuery.fieldName(fieldName)
              .topLeft(topLeft.getLatitude(), topLeft.getLongitude())
              .bottomRight(bottomRight.getLatitude(), bottomRight.getLongitude());
          break;
        default:
          throw new ConversionException("Unsupported search type: " + searchType);
      }
    } else if (methodInvocation.getSource().getExpressionType() == ExpressionType.METHOD_INVOCATION
        && this.query != null) {
      // chain other operation on the query builder
      final Method javaMethod = methodInvocation.getJavaMethod();
      final QueryOperator queryOperator = javaMethod.getAnnotation(QueryOperator.class);
      if (queryOperator != null && queryOperator.value() == QueryOperatorType.BOOST) {
        final float boostFactor = (float) methodInvocation.getArgumentValue(0);
        this.query.boost(boostFactor);
      } else {
        throw new ConversionException("Unsupported method invocation: '"
            + methodInvocation.getJavaMethod().getName() + "' or invalid query operator: " + queryOperator);
      }
    } else if (methodInvocation.getSource().getExpressionType() == ExpressionType.CLASS_LITERAL &&
        (methodInvocation.getJavaMethod().getModifiers() & Modifier.STATIC) == Modifier.STATIC) {
      // ignore here, the value will be retrieved later. 
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
      final Field sourceField = sourceType.getField(field.getFieldName());
      final String fieldName = getDocumentFieldName(sourceField);
      if (fieldName == null) {
        throw new CodecException("Field '" + sourceType.getName() + "." + sourceField.getName()
            + " should be annotated with one of: " + Stream.of(Keyword.class, FullText.class)
                .map(c -> c.getName()).collect(Collectors.joining(", ")));
      }
      if (field.getSource().getExpressionType() == ExpressionType.FIELD_ACCESS) {
        return getDocumentFieldName((FieldAccess) field.getSource()) + "." + fieldName;
      }
      if (fieldName.isEmpty()) {
        // assume that the Elasticsearch document field has the same name as the domain field
        return sourceField.getName();
      }
      return fieldName;
    } catch (NoSuchFieldException e) {
      throw new ConversionException("Failed to retrieve field named '" + field.getFieldName()
          + "' on type '" + sourceType.getName() + "'", e);
    }
  }

  private static String getDocumentFieldName(final Field sourceField) {
    if (sourceField.getAnnotation(DocumentField.class) != null) {
      return sourceField.getAnnotation(DocumentField.class).name();
    }
    return null;
  }


}
