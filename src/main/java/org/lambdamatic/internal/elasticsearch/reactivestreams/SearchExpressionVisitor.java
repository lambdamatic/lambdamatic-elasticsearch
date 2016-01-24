
package org.lambdamatic.internal.elasticsearch.reactivestreams;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.lambdamatic.analyzer.ast.node.CompoundExpression;
import org.lambdamatic.analyzer.ast.node.Expression;
import org.lambdamatic.analyzer.ast.node.ExpressionVisitor;
import org.lambdamatic.analyzer.ast.node.FieldAccess;
import org.lambdamatic.analyzer.ast.node.MethodInvocation;
import org.lambdamatic.elasticsearch.annotations.DocumentField;
import org.lambdamatic.elasticsearch.exceptions.ConversionException;
import org.lambdamatic.elasticsearch.search.SearchExpression;
import org.lambdamatic.internal.elasticsearch.SearchOperation;
import org.lambdamatic.internal.elasticsearch.SearchOperation.EnumSearchType;

/**
 * An {@link ExpressionVisitor} for a {@link SearchExpression} that will prepare the
 * {@link SearchRequest}.
 */
public class SearchExpressionVisitor extends ExpressionVisitor {

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
      final SearchExpressionVisitor operandVisitor = new SearchExpressionVisitor();
      operand.accept(operandVisitor);
      boolQueryBuilder.must(operandVisitor.getQueryBuilder());
    }
    return false;
  }

  @Override
  public boolean visitMethodInvocationExpression(final MethodInvocation methodInvocation) {
    final SearchOperation searchOperation =
        methodInvocation.getJavaMethod().getAnnotation(SearchOperation.class);
    final FieldAccess field = (FieldAccess) methodInvocation.getSource();
    final String fieldName = getDocumentFieldName(field);
    final Expression value = methodInvocation.getArguments().get(0);
    final EnumSearchType searchType = searchOperation.value();
    switch (searchType) {
      case FUZZY:
        this.queryBuilder = QueryBuilders.fuzzyQuery(fieldName, value);
        break;
      case MATCHES:
        break;
      case PROXIMITY:
        break;
      case RANGE:
        break;
      default:
        break;

    }
    return false;
  }

  /**
   * Retrieves the {@link DocumentField#name()} from the given {@link FieldAccess} in a
   * {@link SearchExpression}.
   * 
   * @param field the {@link FieldAccess} in the {@link SearchExpression}
   * @return the name of the field in the Elasticsearch/Lucene document.
   */
  private static String getDocumentFieldName(final FieldAccess field) {
    final Class<?> sourceType = field.getSource().getJavaType();
    try {
      final String fieldName =
          sourceType.getField(field.getFieldName()).getAnnotation(DocumentField.class).name();
      return fieldName;
    } catch (NoSuchFieldException e) {
      throw new ConversionException("Failed to retrieve field named '" + field.getFieldName()
          + "' on type '" + sourceType.getName() + "'", e);
    }
  }

  /**
   * @return the {@link QueryBuilder} corresponding to the {@link SearchExpression} that was
   *         visited.
   */
  public QueryBuilder getQueryBuilder() {
    return this.queryBuilder;
  }

}
