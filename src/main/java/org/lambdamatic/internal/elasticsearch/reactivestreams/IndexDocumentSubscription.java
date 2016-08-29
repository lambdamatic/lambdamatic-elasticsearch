/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.internal.elasticsearch.reactivestreams;

import java.util.concurrent.atomic.AtomicBoolean;

import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseListener;
import org.lambdamatic.internal.elasticsearch.clientdsl.Client;
import org.lambdamatic.internal.elasticsearch.clientdsl.responses.IndexDocumentResponse;
import org.lambdamatic.internal.elasticsearch.codec.CodecRegistry;
import org.lambdamatic.internal.elasticsearch.codec.DocumentCodec;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A <a href= "https://github.com/reactive-streams/reactive-streams-jvm/blob/v1.0.0/README.md">
 * Reactive Streams</a> {@link Subscription} for an <code>Index</code> operation.
 * @param <D> 
 */
public class IndexDocumentSubscription<D> implements Subscription {

  /**
   * the associated {@link Subscriber}.
   */
  final Subscriber<? super IndexDocumentResponse> subscriber;

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
   * The {@link CodecRegistry} to serialize the given document.
   */
  private final CodecRegistry codecRegistry;
  
  /** A flag to indicate if the operation was cancelled. */
  private AtomicBoolean cancelled = new AtomicBoolean(false);

  private JsonFactory jsonFactory;

  /**
   * Constructor.
   * 
   * @param subscriber the {@link Subscriber} for this {@link Subscription}.
   */
  public IndexDocumentSubscription(final Subscriber<? super IndexDocumentResponse> subscriber,
      final Client client, final CodecRegistry codecRegistry, final String indexName,
      final String type, final D document) {
    this.subscriber = subscriber;
    this.client = client;
    this.indexName = indexName;
    this.type = type;
    this.document = document;
    this.codecRegistry = codecRegistry;
    this.jsonFactory = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).getFactory();
  }

  @Override
  public void request(final long n) {
    if (!this.cancelled.get()) {
      final DocumentCodec<D> documentCodec = this.codecRegistry.getDocumentCodec(document);
      final String documentId = documentCodec.getDomainObjectId(document);
      final String jsonDocument = documentCodec.encode(document);
      this.client.index(indexName, type, documentId, jsonDocument, new ResponseListener() {

        @Override
        public void onSuccess(final Response response) {
          final IndexDocumentResponse indexDocumentResponse =
              Client.readResponse(jsonFactory, response, IndexDocumentResponse.class);
          IndexDocumentSubscription.this.subscriber.onNext(indexDocumentResponse);
          IndexDocumentSubscription.this.subscriber.onComplete();
        }

        @Override
        public void onFailure(Exception exception) {
          IndexDocumentSubscription.this.subscriber.onError(exception);
        }
      });
    }
  }

  @Override
  public void cancel() {
    this.cancelled.set(false);
  }

}
