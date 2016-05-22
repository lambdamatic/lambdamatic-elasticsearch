/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.internal.elasticsearch.search;

import java.util.List;
import java.util.stream.Collectors;

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

  /** The usual Logger. */
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(QueryBuilderUtils.class);

  /**
   * Generates the {@link List} of {@link QueryBuilder} from the given {@link QueryExpression}.
   * 
   * @param queryExpression the input {@link QueryExpression}
   * @return the corresponding {@link QueryBuilder}s
   */
  public static List<QueryBuilder> from(final QueryExpression<?> queryExpression) {
    final LambdaExpression lambdaExpression =
        LambdaExpressionAnalyzer.getInstance().analyzeExpression(queryExpression);
    // let's visit the LambdaExpression (an AST) to see how to prepare the search request...
    return lambdaExpression.getBody().stream().map(statement -> (SimpleStatement) statement)
        .map(statement -> {
          final QueryExpressionVisitor queryExpressionVisitor = new QueryExpressionVisitor();
          statement.getExpression().accept(queryExpressionVisitor);
          return queryExpressionVisitor.getQueryBuilder();
        }).collect(Collectors.toList());
  }
}
