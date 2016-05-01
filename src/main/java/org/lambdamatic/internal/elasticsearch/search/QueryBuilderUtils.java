/*******************************************************************************
 * Copyright (c) 2016 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.internal.elasticsearch.search;

import org.elasticsearch.index.query.QueryBuilder;
import org.lambdamatic.analyzer.LambdaExpressionAnalyzer;
import org.lambdamatic.analyzer.ast.node.LambdaExpression;
import org.lambdamatic.analyzer.ast.node.SimpleStatement;
import org.lambdamatic.elasticsearch.querydsl.QueryExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class for {@link QueryBuilder}. 
 */
public class QueryBuilderUtils {

  /** The usual Logger.*/
  private static final Logger LOGGER = LoggerFactory.getLogger(QueryBuilderUtils.class);
  
  /**
   * Generates a {@link QueryBuilder} from the given {@link QueryExpression}.
   * @param searchExpression the input {@link QueryExpression}
   * @return the corresponding {@link QueryBuilder}
   */
  public static QueryBuilder from(final QueryExpression<?> searchExpression) {
    final LambdaExpression lambdaExpression =
        LambdaExpressionAnalyzer.getInstance().analyzeExpression(searchExpression);
    // let's visit the LambdaExpression (an AST) to see how to prepare the search request...
    final SimpleStatement statement = (SimpleStatement) lambdaExpression.getBody().get(0);
    final QueryExpressionVisitor queryExpressionVisitor = new QueryExpressionVisitor();
    statement.getExpression().accept(queryExpressionVisitor);
    final QueryBuilder queryBuilder = queryExpressionVisitor.getQueryBuilder();
    return queryBuilder;
  }
}
