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
import org.lambdamatic.elasticsearch.exceptions.DocumentNotFoundException;
import org.lambdamatic.internal.elasticsearch.clientdsl.Client;
import org.lambdamatic.internal.elasticsearch.clientdsl.responses.GetDocumentResponse;
import org.lambdamatic.internal.elasticsearch.codec.CodecRegistry;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A <a href= "https://github.com/reactive-streams/reactive-streams-jvm/blob/v1.0.0/README.md">
 * Reactive Streams</a> {@link Subscription} for a <code>Get</code> operation.
 * @param <D> 
 */
public class GetDocumentSubscription<D> implements Subscription {

  /**
   * the associated {@link Subscriber}.
   */
  final Subscriber<? super GetDocumentResponse> subscriber;

  /** A flag to indicate if the operation was cancelled. */
  private AtomicBoolean cancelled = new AtomicBoolean(false);

  private Class<D> domainType;

  private Client client;

  private CodecRegistry codecRegistry;

  private String indexName;

  private String type;

  private String documentId;

  private JsonFactory jsonFactory;

  /**
   * Constructor.
   * 
   * @param subscriber the {@link Subscriber} for this {@link Subscription}.
   */
  public GetDocumentSubscription(final Subscriber<? super GetDocumentResponse> subscriber,
      final Client client, final CodecRegistry codecRegistry, final String indexName,
      final String type, final String documentId, final Class<D> domainType) {
    this.subscriber = subscriber;
    this.client = client;
    this.codecRegistry = codecRegistry;
    this.indexName = indexName;
    this.type = type;
    this.documentId = documentId;
    this.domainType = domainType;
    this.jsonFactory = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).getFactory();
  }

  @Override
  public void request(final long n) {
    if (!this.cancelled.get()) {
      this.client.getDocument(indexName, type, documentId, new ResponseListener() {

        @Override
        public void onSuccess(final Response response) {
          final GetDocumentResponse getDocumentResponse =
              Client.readResponse(jsonFactory, response, GetDocumentResponse.class);
          if (getDocumentResponse.isExists()) {
            GetDocumentSubscription.this.subscriber.onNext(getDocumentResponse);
            GetDocumentSubscription.this.subscriber.onComplete();
          } else {
            GetDocumentSubscription.this.subscriber.onNext(null);
            GetDocumentSubscription.this.subscriber.onComplete();
          }
        }

        @Override
        public void onFailure(Exception exception) {
          GetDocumentSubscription.this.subscriber.onError(exception);
        }
      });
    }
      
      

    
    
  }

  @Override
  public void cancel() {
    this.cancelled.set(false);
  }

}
