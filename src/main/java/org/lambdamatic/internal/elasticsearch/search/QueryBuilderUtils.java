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
import org.lambdamatic.elasticsearch.search.SearchExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class for {@link QueryBuilder}. 
 */
public class QueryBuilderUtils {

  /** The usual Logger.*/
  private static final Logger LOGGER = LoggerFactory.getLogger(QueryBuilderUtils.class);
  
  /**
   * Generates a {@link QueryBuilder} from the given {@link SearchExpression}.
   * @param searchExpression the input {@link SearchExpression}
   * @return the corresponding {@link QueryBuilder}
   */
  public static QueryBuilder from(final SearchExpression<?> searchExpression) {
    final LambdaExpression lambdaExpression =
        LambdaExpressionAnalyzer.getInstance().analyzeExpression(searchExpression);
    LOGGER.debug("Preparing a SearchRequest based on {}", lambdaExpression);
    // let's visit the LambdaExpression (an AST) to see how to prepare the search request...
    final SearchExpressionVisitor searchExpressionVisitor = new SearchExpressionVisitor();
    final SimpleStatement statement = (SimpleStatement) lambdaExpression.getBody().get(0);
    statement.getExpression().accept(searchExpressionVisitor);
    final QueryBuilder queryBuilder = searchExpressionVisitor.getQueryBuilder();
    LOGGER.debug(" search query: {}", queryBuilder.toString());
    return queryBuilder;
  }
}
