/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.internal.elasticsearch.reactivestreams;

import org.lambdamatic.internal.elasticsearch.clientdsl.Client;
import org.lambdamatic.internal.elasticsearch.clientdsl.responses.GetDocumentResponse;
import org.lambdamatic.internal.elasticsearch.codec.CodecRegistry;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

/**
 * A <a href= "https://github.com/reactive-streams/reactive-streams-jvm/blob/v1.0.0/README.md">
 * Reactive Streams</a> {@link Publisher} for a <code>Get</code> operation.
 */
public class GetDocumentPublisher<D> implements Publisher<GetDocumentResponse> {

  /**
   * The Elasticsearch {@link Client}.
   */
  private final Client client;

  /** The name of the index in which the <code>Get</code> operation will be performed. */
  private final String indexName;

  /** The type of document to get. */
  private final String type;

  /** The id of the document to get. */
  private final String documentId;

  private final CodecRegistry codecRegistry;

  private Class<D> domainType;

  /**
   * Constructor.
   * 
   * @param client the Elasticsearch {@link Client}
   * @param indexName the name of the index in which the <code>Get</code> operation will be
   *        performed.
   * @param type the type of document to get.
   * @param documentId the id of the document to get.
   */
  public GetDocumentPublisher(final Client client, final CodecRegistry codecRegistry, final String indexName, final String type,
      final String documentId, final Class<D> domainType) {
    this.client = client;
    this.codecRegistry = codecRegistry;
    this.indexName = indexName;
    this.type = type;
    this.documentId = documentId;
    this.domainType = domainType;
  }

  public void subscribe(Subscriber<? super GetDocumentResponse> subscriber) {
    final GetDocumentSubscription<D> subscription = new GetDocumentSubscription<>(subscriber,
        this.client, this.codecRegistry, this.indexName, this.type, this.documentId, this.domainType);
    subscriber.onSubscribe(subscription);
  }

}
