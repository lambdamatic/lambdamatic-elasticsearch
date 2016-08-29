/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.internal.elasticsearch.searchdsl;

import org.lambdamatic.elasticsearch.searchdsl.QueryExpression;
import org.lambdamatic.internal.elasticsearch.clientdsl.Client;

/**
 * The top-level (root) {@link Query} element. It that can be serialized into a JSON document and
 * sent by the {@link Client} to the Elasticsearch cluster.
 * 
 */
public class DocumentSearch {

  private final Query shouldMatchQuery;

  private final Query mustMatchQuery;
  
  private final Query mustNotMatchQuery;
  
  private final Query filterQuery;

  public DocumentSearch(final QueryExpression<?> shouldMatchExpression,
      final QueryExpression<?> mustMatchExpression,
      final QueryExpression<?> mustNotMatchExpression,
      // TODO: we may need to (re)introduce a FilterExpression type to limit the DSL to term
      // queries, etc. and avoid exposing 'match' queries in the filter context.
      final QueryExpression<?> filterExpression) {
    this.shouldMatchQuery = QueryUtils.getQuery(shouldMatchExpression);
    this.mustMatchQuery = QueryUtils.getQuery(mustMatchExpression);
    this.mustNotMatchQuery = QueryUtils.getQuery(mustNotMatchExpression);
    this.filterQuery = QueryUtils.getQuery(filterExpression);
  }

  public Query getShouldMatchQuery() {
    return this.shouldMatchQuery;
  }

  public Query getMustMatchQuery() {
    return this.mustMatchQuery;
  }
  
  public Query getMustNotMatchQuery() {
    return this.mustNotMatchQuery;
  }
  
  public Query getFilterQuery() {
    return this.filterQuery;
  }

  @Override
  public String toString() {
    return "DocumentSearch [shouldMatchQuery=" + shouldMatchQuery + ", mustMatchQuery="
        + mustMatchQuery + ", mustNotMatchQuery=" + mustNotMatchQuery + ", filterQuery="
        + filterQuery + "]";
  }
  
  

}
