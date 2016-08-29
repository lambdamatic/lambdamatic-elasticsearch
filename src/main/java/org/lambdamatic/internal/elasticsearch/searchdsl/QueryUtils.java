/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.internal.elasticsearch.searchdsl;

import org.lambdamatic.analyzer.LambdaExpressionAnalyzer;
import org.lambdamatic.analyzer.ast.node.LambdaExpression;
import org.lambdamatic.analyzer.ast.node.SimpleStatement;
import org.lambdamatic.elasticsearch.searchdsl.QueryExpression;

/**
 * A utility class to get a {@link Query} from a {@link QueryExpression}.
 */
public class QueryUtils {

  public static Query getQuery(final QueryExpression<?> queryExpression) {
    return analyseExpression(queryExpression);
  }

  private static Query analyseExpression(final Object expression) {
    if (expression == null) {
      return null;
    }
    final LambdaExpression lambdaExpression =
        LambdaExpressionAnalyzer.getInstance().analyzeExpression(expression);
    // let's visit the LambdaExpression (an AST) to see how to prepare the search request...
    final SimpleStatement statement = (SimpleStatement) lambdaExpression.getBody().get(0);
    final QueryExpressionVisitor queryExpressionVisitor = new QueryExpressionVisitor();
    statement.getExpression().accept(queryExpressionVisitor);
    return queryExpressionVisitor.getQuery();
  }



}
