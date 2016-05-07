/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.internal.elasticsearch;

import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collector;
import java.util.stream.StreamSupport;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.lambdamatic.elasticsearch.ElasticsearchDomainTypeManager;
import org.lambdamatic.elasticsearch.annotations.Document;
import org.lambdamatic.elasticsearch.querydsl.Collectable;
import org.lambdamatic.elasticsearch.querydsl.FilterContext;
import org.lambdamatic.elasticsearch.querydsl.MustMatchContext;
import org.lambdamatic.elasticsearch.querydsl.QueryExpression;
import org.lambdamatic.internal.elasticsearch.search.QueryBuilderUtils;
import org.lambdamatic.internal.elasticsearch.search.streams.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The base implementation class for all generated entity managers.
 * 
 * @param <D> the type of the domain document.
 * @param <Q> the query type associated with the domain document.
 * 
 */
public abstract class BaseElasticsearchDomainTypeManagerImpl<D, Q extends QueryMetadata<D>>
    implements ElasticsearchDomainTypeManager<D, Q> {

  /** The usual Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(BaseElasticsearchDomainTypeManagerImpl.class);

  /**
   * the underlying {@link Client} to connect to the Elasticsearch cluster.
   */
  private final Client client;

  /** The domain type associated with this index. */
  private final Class<D> domainType;

  /** Name of the index in Elasticsearch. */
  private final String indexName;

  /** Value of the <code>_type</code> field to categorize the document in the Elasticsearch. */
  private final String type;

  private final IndexMappingValidator mappingValidator;

  /** The expression to use to <strong>match (SHOULD)</strong> documents. */
  private QueryExpression<Q> shouldMatchExpression;

  /** The expression to use to <strong>match (MUST)</strong> documents. */
  private QueryExpression<Q> mustMatchExpression;
  
  /** The expression to use to <strong>filter</strong> documents. */
  private QueryExpression<Q> filterExpression;
  
  /**
   * Constructor.
   * 
   * @param client The underlying {@link Client} to connect to the Elasticsearch cluster
   * @param domainType The domain type associated with this index
   */
  public BaseElasticsearchDomainTypeManagerImpl(final Client client, final Class<D> domainType) {
    this.client = client;
    this.domainType = domainType;
    final Document documentAnnotation = domainType.getAnnotation(Document.class);
    if (documentAnnotation == null) {
      throw new IllegalStateException("Class '" + domainType.getName() + "' is missing the '"
          + Document.class.getName() + "' annotation.");
    }
    this.indexName = documentAnnotation.index();
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
  public Class<D> getDomainType() {
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
  public MustMatchContext<D, Q> shouldMatch(final QueryExpression<Q> filterExpression) {
    this.shouldMatchExpression = Objects.requireNonNull(filterExpression);
    return this;
  }

  @Override
  public FilterContext<D, Q> mustMatch(final QueryExpression<Q> filterExpression) {
    this.mustMatchExpression = Objects.requireNonNull(filterExpression);
    return this;
  }
  
  @Override
  public Collectable<D, Q> filter(final QueryExpression<Q> filterExpression) {
    this.filterExpression = Objects.requireNonNull(filterExpression);
    return this;
  }
  
  @Override
  public <R, A> R collect(final Collector<? super D, A, R> collector) {
    LOGGER.debug("Executing query...");
    final SearchRequestBuilder requestBuilder = this.client.prepareSearch(this.indexName)
        .setTypes(this.type);
    
    final BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
    if (this.shouldMatchExpression != null) {
      QueryBuilderUtils.from(this.shouldMatchExpression).stream().forEach(queryBuilder::should);
    }
    if (this.mustMatchExpression != null) {
      QueryBuilderUtils.from(this.mustMatchExpression).stream().forEach(queryBuilder::must);
    }
    if (this.filterExpression != null) {
      QueryBuilderUtils.from(this.filterExpression).stream().forEach(queryBuilder::filter);
    }
    requestBuilder.setQuery(queryBuilder);
    LOGGER.debug("Query: {}", requestBuilder.toString());
    final SearchResponse response = requestBuilder.execute().actionGet();
    LOGGER.trace("Query response: {}", response);
    final Iterator<SearchHit> iterator = response.getHits().iterator();
    final Spliterator<SearchHit> spliterator =
        Spliterators.spliteratorUnknownSize(iterator, Spliterator.IMMUTABLE);
    return StreamSupport.stream(spliterator, false)
        .map(searchHit -> Converter.toDomainType(this.domainType, searchHit.getId(), searchHit.sourceAsMap())
        ).collect(collector);
  }
}
