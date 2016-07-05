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
import java.util.function.Consumer;
import java.util.stream.Collector;
import java.util.stream.StreamSupport;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.lambdamatic.elasticsearch.ElasticsearchDomainTypeManager;
import org.lambdamatic.elasticsearch.annotations.Document;
import org.lambdamatic.elasticsearch.exceptions.DomainTypeException;
import org.lambdamatic.elasticsearch.querydsl.Collectable;
import org.lambdamatic.elasticsearch.querydsl.FilterContext;
import org.lambdamatic.elasticsearch.querydsl.MustMatchContext;
import org.lambdamatic.elasticsearch.querydsl.QueryExpression;
import org.lambdamatic.internal.elasticsearch.codec.DocumentCodec;
import org.lambdamatic.internal.elasticsearch.reactivestreams.GetPublisher;
import org.lambdamatic.internal.elasticsearch.reactivestreams.IndexPublisher;
import org.lambdamatic.internal.elasticsearch.search.QueryBuilderUtils;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

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
  private static final Logger LOGGER =
      LoggerFactory.getLogger(BaseElasticsearchDomainTypeManagerImpl.class);

  /**
   * the underlying {@link Client} to connect to the Elasticsearch cluster.
   */
  private final Client client;

  /**
   * The underlying {@link ObjectMapper} to handle serialization and deserialization of the domain
   * objects.
   */
  private final ObjectMapper objectMapper;

  /** The domain type associated with this index. */
  private final Class<D> domainType;

  /** Name of the index in Elasticsearch. */
  private final String indexName;

  /** Value of the <code>_type</code> field to categorize the document in the Elasticsearch. */
  private final String type;

  /** The mapping validator. */
  private final IndexMappingValidator mappingValidator;

  /** The document codec. */
  private final DocumentCodec<D> documentCodec;

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
   * @param objectMapper The configured {@link ObjectMapper}
   * @param domainType The domain type associated with this index
   * @throws DomainTypeException if something went wrong during the introspection of the given
   *         domain type
   */
  public BaseElasticsearchDomainTypeManagerImpl(final Client client,
      final ObjectMapper objectMapper, final Class<D> domainType) throws DomainTypeException {
    this.client = client;
    this.objectMapper = objectMapper;
    this.domainType = domainType;
    final Document documentAnnotation = domainType.getAnnotation(Document.class);
    if (documentAnnotation == null) {
      throw new IllegalStateException("Class '" + domainType.getName() + "' is missing the '"
          + Document.class.getName() + "' annotation.");
    }
    this.indexName = documentAnnotation.index();
    this.type = documentAnnotation.type();
    this.mappingValidator =
        new IndexMappingValidator(this.client, this.domainType, this.indexName, this.type);
    this.documentCodec = new DocumentCodec<>(this.domainType, this.objectMapper);

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
  public void index(final D document) {
    final String jsonDocument = this.documentCodec.encode(document);
    final String documentId = this.documentCodec.getDomainObjectId(document);
    if (documentId != null) {
      client.prepareIndex(indexName, type, documentId).setSource(jsonDocument).get();
    } else {
      final IndexResponse indexResponse =
          client.prepareIndex(indexName, type).setSource(jsonDocument).get();
      // id was generated and must now be set back in the given document
      this.documentCodec.setDomainObjectId(document, indexResponse.getId());
    }
  }

  @Override
  public void asyncIndex(final D document, final Consumer<D> onSuccess,
      final Consumer<Throwable> onError) {
    final IndexPublisher<D> publisher =
        new IndexPublisher<>(this.client, this.documentCodec, this.indexName, this.type, document);
    publisher.subscribe(new Subscriber<IndexResponse>() {

      @Override
      public void onSubscribe(final Subscription s) {
        s.request(1);
      }

      @Override
      public void onNext(final IndexResponse t) {
        onSuccess.accept(document);
      }

      @Override
      public void onError(Throwable t) {
        onError.accept(t);
      }

      @Override
      public void onComplete() {
        // do nothing, we are only collecting a single element.
      }
    });

  }

  @Override
  public D get(final String documentId) {
    final GetResponse getResponse =
        this.client.prepareGet(this.indexName, this.type, documentId).get();
    return this.documentCodec.decode(getResponse.getId(), getResponse.getSourceAsString());
  }

  @Override
  public void asyncGet(final String documentId, Consumer<D> onSuccess,
      Consumer<Throwable> onError) {
    final GetPublisher publisher =
        new GetPublisher(this.client, this.indexName, this.type, documentId);
    publisher.subscribe(new Subscriber<GetResponse>() {

      @Override
      public void onSubscribe(final Subscription s) {
        s.request(1);
      }

      @Override
      public void onNext(final GetResponse t) {
        final D document = documentCodec.decode(t.getId(), t.getSourceAsString());
        onSuccess.accept(document);
      }

      @Override
      public void onError(Throwable t) {
        onError.accept(t);
      }

      @Override
      public void onComplete() {
        // do nothing, we are only collecting a single element.
      }
    });
  }

  @Override
  public MustMatchContext<D, Q> shouldMatch(final QueryExpression<Q> shouldMatchExpression) {
    this.shouldMatchExpression =
        Objects.requireNonNull(shouldMatchExpression, "ShouldMatch expression must not be null");
    return this;
  }

  @Override
  public FilterContext<D, Q> mustMatch(final QueryExpression<Q> mustMatchExpression) {
    this.mustMatchExpression =
        Objects.requireNonNull(mustMatchExpression, "MustMatch expression must not be null");
    return this;
  }

  @Override
  public Collectable<D, Q> filter(final QueryExpression<Q> filterExpression) {
    this.filterExpression =
        Objects.requireNonNull(filterExpression, "Filter expression must not be null");
    return this;
  }

  @Override
  public <R, A> R collect(final Collector<? super D, A, R> collector) {
    LOGGER.debug("Executing query...");
    final SearchRequestBuilder requestBuilder =
        this.client.prepareSearch(this.indexName).setTypes(this.type);

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
        .map(searchHit -> this.documentCodec.decode(searchHit.getId(), searchHit.sourceAsString()))
        .collect(collector);
  }

}
