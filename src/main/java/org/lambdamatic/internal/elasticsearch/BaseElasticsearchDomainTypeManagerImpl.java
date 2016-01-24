/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.internal.elasticsearch;

import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.lambdamatic.elasticsearch.ElasticsearchDomainTypeManager;
import org.lambdamatic.elasticsearch.annotations.Document;
import org.lambdamatic.elasticsearch.search.SearchExpression;
import org.lambdamatic.internal.elasticsearch.reactivestreams.GetPublisher;
import org.lambdamatic.internal.elasticsearch.reactivestreams.SearchPublisher;
import org.reactivestreams.Publisher;

/**
 * The base implementation class for all generated entity managers.
 * 
 * @param <DomainType> the type of the domain document.
 * @param <QueryType> the query type associated with the domain document.
 * 
 */
public abstract class BaseElasticsearchDomainTypeManagerImpl<DomainType, QueryType extends QueryMetadata<DomainType>>
    implements ElasticsearchDomainTypeManager<DomainType, QueryType> {

  /**
   * the underlying {@link Client} to connect to the Elasticsearch cluster.
   */
  private final Client client;

  /** The domain type associated with this index. */
  private final Class<DomainType> domainType;

  /** Name of the index in Elasticsearch. */
  private final String indexName;

  /** Value of the <code>_type</code> field to categorize the document in the Elasticsearch. */
  private final String type;

  private final IndexMappingValidator mappingValidator;

  /**
   * Constructor.
   * 
   * @param client The underlying {@link Client} to connect to the Elasticsearch cluster
   * @param domainType The domain type associated with this index
   */
  public BaseElasticsearchDomainTypeManagerImpl(final Client client, final Class<DomainType> domainType) {
    this.client = client;
    this.domainType = domainType;
    final Document documentAnnotation = domainType.getAnnotation(Document.class);
    if (documentAnnotation == null) {
      throw new IllegalStateException("Class '" + domainType.getName() + "' is missing the '"
          + Document.class.getName() + "' annotation.");
    }
    this.indexName = documentAnnotation.indexName();
    this.type = documentAnnotation.type();
    this.mappingValidator = new IndexMappingValidator(this.client, this.domainType, this.indexName, this.type);
  }

  /**
   * @return the name of the index in Elasticsearch.
   */
  public String getIndexName() {
    return this.indexName;
  }

  /**
   * @return the associated Domain type.
   */
  public Class<DomainType> getDomainType() {
    return this.domainType;
  }

  /**
   * @return the value of the <code>_type</code> Lucene document field for the associated Domain
   *         type.
   */
  public String getType() {
    return this.type;
  }

  /**
   * Verifies the status of the corresponding index in Elasticsearch.
   * 
   * @return the index status
   * @see IndexMappingValidator#verifyIndex()
   */
  public IndexValidationStatus verifyIndex() {
    return this.mappingValidator.verifyIndex();
  }

  @Override
  public Publisher<IndexResponse> index(final DomainType document) {
    return null;
  }

  @Override
  public Publisher<GetResponse> get(final String documentId) {
    return new GetPublisher(this.client, this.indexName, this.type, documentId);
  }

  @Override
  public Publisher<DeleteResponse> delete(final Object documentId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Publisher<SearchResponse> search(final SearchExpression<QueryType> expression) {
    return new SearchPublisher<>(this.client, this.indexName, this.type, expression);
  }

}
