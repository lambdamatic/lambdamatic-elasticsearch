/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.internal.elasticsearch.reactivestreams;

import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.lambdamatic.internal.elasticsearch.codec.DocumentCodec;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

/**
 * A <a href= "https://github.com/reactive-streams/reactive-streams-jvm/blob/v1.0.0/README.md">
 * Reactive Streams</a> {@link Publisher} for an <code>Index</code> operation.
 * 
 * @param <D> the type of document to index
 */
public class IndexPublisher<D> implements Publisher<IndexResponse> {

  /**
   * The Elasticsearch {@link Client}.
   */
  private final Client client;

  /** The name of the index in which the <code>Index</code> operation will be performed. */
  private final String indexName;

  /** The type of document to Index. */
  private final String type;

  /** The document to Index. */
  private final D document;

  /**
   * Constructor.
   * 
   * @param client the Elasticsearch {@link Client}
   * @param indexName the name of the index in which the <code>Index</code> operation will be
   *        performed.
   * @param type the type of document to Index.
   * @param document the document to Index.
   */
  public IndexPublisher(final Client client, final String indexName, final String type,
      final D document) {
    this.client = client;
    this.indexName = indexName;
    this.type = type;
    this.document = document;
  }

  @Override
  public void subscribe(final Subscriber<? super IndexResponse> subscriber) {
    DocumentCodec<D> documentCodec = new DocumentCodec<>((Class<D>) document.getClass());
    @SuppressWarnings("unchecked")
    final String documentId = documentCodec.getDomainObjectId(this.document);
    final IndexRequestBuilder requestBuilder =
        this.client.prepareIndex(this.indexName, this.type, documentId).setSource(documentCodec.toSourceMap(document));
    final IndexSubscription subscription = new IndexSubscription(subscriber, requestBuilder);
    subscriber.onSubscribe(subscription);
  }

}
