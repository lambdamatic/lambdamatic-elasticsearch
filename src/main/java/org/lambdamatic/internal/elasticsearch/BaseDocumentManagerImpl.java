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

import org.lambdamatic.elasticsearch.DocumentManagement;
import org.lambdamatic.elasticsearch.DocumentQuery;
import org.lambdamatic.elasticsearch.annotations.Document;
import org.lambdamatic.elasticsearch.exceptions.DocumentNotFoundException;
import org.lambdamatic.elasticsearch.exceptions.DomainTypeException;
import org.lambdamatic.elasticsearch.searchdsl.CollectableContext;
import org.lambdamatic.elasticsearch.searchdsl.MustMatchContext;
import org.lambdamatic.elasticsearch.searchdsl.MustNotMatchContext;
import org.lambdamatic.elasticsearch.searchdsl.QueryExpression;
import org.lambdamatic.elasticsearch.searchdsl.ShouldMatchContext;
import org.lambdamatic.internal.elasticsearch.clientdsl.Client;
import org.lambdamatic.internal.elasticsearch.clientdsl.responses.GetDocumentResponse;
import org.lambdamatic.internal.elasticsearch.clientdsl.responses.IndexDocumentResponse;
import org.lambdamatic.internal.elasticsearch.clientdsl.responses.SearchResponse;
import org.lambdamatic.internal.elasticsearch.clientdsl.responses.SearchResponse.SearchHit;
import org.lambdamatic.internal.elasticsearch.codec.CodecRegistry;
import org.lambdamatic.internal.elasticsearch.codec.DocumentCodec;
import org.lambdamatic.internal.elasticsearch.codec.DocumentSearchCodec;
import org.lambdamatic.internal.elasticsearch.reactivestreams.GetDocumentPublisher;
import org.lambdamatic.internal.elasticsearch.reactivestreams.GetDocumentResponseSubscriber;
import org.lambdamatic.internal.elasticsearch.reactivestreams.IndexDocumentPublisher;
import org.lambdamatic.internal.elasticsearch.reactivestreams.IndexDocumentResponseSubscriber;
import org.lambdamatic.internal.elasticsearch.searchdsl.DocumentSearch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The base implementation class for all generated document managers.
 * 
 * @param <D> the type of the domain document.
 * @param <Q> the {@link QueryMetadata} type associated with the type of documents.
 * 
 */
public abstract class BaseDocumentManagerImpl<D, Q extends QueryMetadata<D>>
    implements DocumentManagement<D>, DocumentQuery<D, Q> {

  /** The usual Logger. */
  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(BaseDocumentManagerImpl.class);

  /**
   * the underlying {@link Client} to connect to the Elasticsearch cluster.
   */
  private final Client client;

  /** The domain type associated with this index. */
  private final Class<D> domainType;

  private final CodecRegistry codecRegistry = new CodecRegistry();

  /** Name of the index in Elasticsearch. */
  private final String indexName;

  /** Value of the <code>_type</code> field to categorize the document in the Elasticsearch. */
  private final String type;

  /** The mapping validator. */
  private final IndexMappingService mappingValidator;

  /**
   * Constructor.
   * 
   * @param client The underlying {@link Client} to connect to the Elasticsearch cluster
   * @param domainType The domain type associated with this index
   * @throws DomainTypeException if something went wrong during the introspection of the given
   *         domain type
   */
  public BaseDocumentManagerImpl(final Client client, final Class<D> domainType)
      throws DomainTypeException {
    this.domainType = domainType;
    this.client = client;
    this.codecRegistry.registerCodec(this.domainType);
    final Document documentAnnotation = domainType.getAnnotation(Document.class);
    if (documentAnnotation == null) {
      throw new IllegalStateException("Class '" + domainType.getName() + "' is missing the '"
          + Document.class.getName() + "' annotation.");
    }
    this.indexName = documentAnnotation.index();
    this.type = documentAnnotation.type();
    this.mappingValidator =
        new IndexMappingService(this.client, this.domainType, this.indexName, this.type);
  }

  /**
   * @deprecated we should retrieve the appropriate {@link DocumentCodec} from the _source field.
   */
  private DocumentCodec<D> getDefaultCodec() {
    return this.codecRegistry.getDocumentCodec(this.domainType);
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
   * @see IndexMappingService#verifyIndex()
   */
  public IndexValidationStatus verifyIndex() {
    return this.mappingValidator.verifyIndex();
  }

  @Override
  public void index(final D document) {
    final DocumentCodec<D> documentCodec = this.codecRegistry.getDocumentCodec(document);
    final String jsonDocument = documentCodec.encode(document);
    final String documentId = documentCodec.getDomainObjectId(document);
    final IndexDocumentResponse indexDocumentResponse =
        client.index(this.indexName, this.type, documentId, jsonDocument);
    if (documentId == null) {
      documentCodec.setDomainObjectId(document, indexDocumentResponse.getId());
    }
  }

  @Override
  public void asyncIndex(final D document, final Consumer<D> onSuccessHandler,
      final Consumer<Throwable> onErrorHandler) {
    final IndexDocumentPublisher<D> publisher = new IndexDocumentPublisher<>(this.client,
        this.codecRegistry, this.indexName, this.type, document);
    publisher.subscribe(new IndexDocumentResponseSubscriber<>(document, this.codecRegistry,
        onSuccessHandler, onErrorHandler));
  }

  @Override
  public D get(final String documentId) {
    final GetDocumentResponse getDocumentResponse =
        client.getDocument(this.indexName, this.type, documentId);
    if (getDocumentResponse.isExists()) {
      return this.codecRegistry.getDocumentCodec(getDocumentResponse.getSource(), this.domainType)
          .decode(documentId, getDocumentResponse.getSource());
    }
    throw new DocumentNotFoundException(this.indexName, this.type, documentId);
  }

  @Override
  public void asyncGet(final String documentId, Consumer<D> onSuccessHandler,
      Consumer<Throwable> onErrorHandler) {
    final GetDocumentPublisher<D> publisher = new GetDocumentPublisher<>(this.client,
        this.codecRegistry, this.indexName, this.type, documentId, this.domainType);
    publisher.subscribe(new GetDocumentResponseSubscriber<>(this.codecRegistry, this.indexName,
        this.type, documentId, this.domainType, onSuccessHandler, onErrorHandler));

  }

  @Override
  // TODO: replace the returned type 'DocumentSearchDelegate' with something else to avoid exposing
  // the whole DocumentSearchDelegate API ?
  public ShouldMatchContext<D, Q> shouldMatch(final QueryExpression<Q> shoudMatchExpression) {
    return DocumentSearchDelegate.shouldMatch(this,
        Objects.requireNonNull(shoudMatchExpression, "'Should Match' expression must not be null"));
  }

  @Override
  public MustMatchContext<D, Q> mustMatch(final QueryExpression<Q> mustMatchExpression) {
    return DocumentSearchDelegate.mustMatch(this,
        Objects.requireNonNull(mustMatchExpression, "'Must Match' expression must not be null"));
  }

  @Override
  public MustNotMatchContext<D, Q> mustNotMatch(final QueryExpression<Q> mustNotMatchExpression) {
    return DocumentSearchDelegate.mustNotMatch(this, Objects.requireNonNull(mustNotMatchExpression,
        "'Should Not Match' expression must not be null"));
  }

  @Override
  public CollectableContext<D, Q> filter(final QueryExpression<Q> filterExpression) {
    return DocumentSearchDelegate.filter(this,
        Objects.requireNonNull(filterExpression, "'Filter' expression must not be null"));
  }

  public static class DocumentSearchDelegate<D, Q extends QueryMetadata<D>>
      implements ShouldMatchContext<D, Q> {

    private final BaseDocumentManagerImpl<D, Q> parent;

    private QueryExpression<Q> shouldMatchExpression;

    private QueryExpression<Q> mustMatchExpression;

    private QueryExpression<Q> mustNotMatchExpression;

    private QueryExpression<Q> filterExpression;

    private DocumentSearchDelegate(final BaseDocumentManagerImpl<D, Q> parent) {
      this.parent = parent;
    }

    public static <D, Q extends QueryMetadata<D>> DocumentSearchDelegate<D, Q> shouldMatch(
        final BaseDocumentManagerImpl<D, Q> parent,
        final QueryExpression<Q> shouldMatchExpression) {
      final DocumentSearchDelegate<D, Q> delegate = new DocumentSearchDelegate<>(parent);
      delegate.shouldMatchExpression = shouldMatchExpression;
      return delegate;
    }

    public static <D, Q extends QueryMetadata<D>> DocumentSearchDelegate<D, Q> mustMatch(
        final BaseDocumentManagerImpl<D, Q> parent, final QueryExpression<Q> mustMatchExpression) {
      final DocumentSearchDelegate<D, Q> delegate = new DocumentSearchDelegate<>(parent);
      delegate.mustMatchExpression = mustMatchExpression;
      return delegate;
    }

    @Override
    public MustMatchContext<D, Q> mustMatch(final QueryExpression<Q> mustMatchExpression) {
      this.mustMatchExpression = mustMatchExpression;
      return this;
    }

    public static <D, Q extends QueryMetadata<D>> DocumentSearchDelegate<D, Q> mustNotMatch(
        final BaseDocumentManagerImpl<D, Q> parent,
        final QueryExpression<Q> mustNotMatchExpression) {
      final DocumentSearchDelegate<D, Q> delegate = new DocumentSearchDelegate<>(parent);
      delegate.mustNotMatchExpression = mustNotMatchExpression;
      return delegate;
    }

    @Override
    public MustNotMatchContext<D, Q> mustNotMatch(final QueryExpression<Q> mustNotMatchExpression) {
      this.mustNotMatchExpression = mustNotMatchExpression;
      return this;
    }

    public static <D, Q extends QueryMetadata<D>> DocumentSearchDelegate<D, Q> filter(
        final BaseDocumentManagerImpl<D, Q> parent, final QueryExpression<Q> filterExpression) {
      final DocumentSearchDelegate<D, Q> delegate = new DocumentSearchDelegate<>(parent);
      delegate.filterExpression = filterExpression;
      return delegate;
    }

    @Override
    public CollectableContext<D, Q> filter(final QueryExpression<Q> filterExpression) {
      this.filterExpression = filterExpression;
      return this;
    }

    @Override
    public <R, A> R collect(final Collector<? super D, A, R> collector) {
      LOGGER.debug("Executing query...");
      final DocumentSearch documentSearch = new DocumentSearch(this.shouldMatchExpression,
          this.mustMatchExpression, this.mustNotMatchExpression, this.filterExpression);
      final DocumentSearchCodec documentSearchCodec = parent.codecRegistry.getDocumentQueryCodec();
      final SearchResponse<D> response = parent.client.search(parent.indexName, parent.type,
          documentSearchCodec.encode(documentSearch));
      LOGGER.trace("Query response: {} total hits", response.getTotalHits());
      final Iterator<SearchHit> iterator = response.getSearchHits().iterator();
      final Spliterator<SearchHit> spliterator =
          Spliterators.spliteratorUnknownSize(iterator, Spliterator.IMMUTABLE);
      return StreamSupport.stream(spliterator, false).map(searchHit -> {
        final DocumentCodec<D> documentCodec =
            parent.codecRegistry.getDocumentCodec(searchHit.getSource(), parent.domainType);
        return documentCodec.decode(searchHit.getId(), searchHit.getSource());
      }).collect(collector);
    }



  }


}
