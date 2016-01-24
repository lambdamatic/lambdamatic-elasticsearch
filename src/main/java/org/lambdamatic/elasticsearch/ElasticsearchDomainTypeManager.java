/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.elasticsearch;

import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.lambdamatic.elasticsearch.search.SearchExpression;
import org.lambdamatic.internal.elasticsearch.QueryMetadata;
import org.reactivestreams.Publisher;

/**
 * Interface to manage data in Elasticsearch Indexes.
 * 
 * @param <DomainType> the type of documents stored in the associated index.
 * @param <QueryType> the {@link QueryMetadata} type associated with the <code>DomainType</code>
 */
public interface ElasticsearchDomainTypeManager<DomainType, QueryType extends QueryMetadata<DomainType>> {

  /**
   * Adds the given document in the index
   * 
   * @param document the document to add
   * @return A <a href=
   *         "https://github.com/reactive-streams/reactive-streams-jvm/blob/v1.0.0/README.md">
   *         Reactive Streams</a> {@link Publisher} for the {@link IndexResponse} returned by the
   *         underlying {@link Client}.
   */
  public Publisher<IndexResponse> index(DomainType document);

  /**
   * The get API allows to get a typed JSON document from the index based on its id.
   * 
   * @param documentId the id of the document to get
   * @return A <a href=
   *         "https://github.com/reactive-streams/reactive-streams-jvm/blob/v1.0.0/README.md">
   *         Reactive Streams</a> {@link Publisher} for the {@link GetResponse} returned by the
   *         underlying {@link Client}.
   */
  public Publisher<GetResponse> get(String documentId);

  /**
   * The delete API allows one to delete a typed JSON document from a specific index based on its
   * id.
   * 
   * @param documentId the id of the document to delete
   * 
   * @return A <a href=
   *         "https://github.com/reactive-streams/reactive-streams-jvm/blob/v1.0.0/README.md">
   *         Reactive Streams</a> {@link Publisher} for the {@link DeleteResponse} returned by the
   *         underlying {@link Client}.
   */
  public Publisher<DeleteResponse> delete(Object documentId);

  /**
   * Searches for documents in the index associated with the domain type.
   * 
   * @param expression the search expression
   * @return A <a href=
   *         "https://github.com/reactive-streams/reactive-streams-jvm/blob/v1.0.0/README.md">
   *         Reactive Streams</a> {@link Publisher} for the domain-type specific search result.
   */
  public Publisher<SearchResponse> search(SearchExpression<QueryType> expression);
}
