/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.internal.elasticsearch.reactivestreams;

import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

/**
 * A <a href= "https://github.com/reactive-streams/reactive-streams-jvm/blob/v1.0.0/README.md">
 * Reactive Streams</a> {@link Publisher} for a <code>Get</code> operation.
 */
public class GetPublisher implements Publisher<GetResponse> {

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

  /**
   * Constructor.
   * 
   * @param client the Elasticsearch {@link Client}
   * @param indexName the name of the index in which the <code>Get</code> operation will be
   *        performed.
   * @param type the type of document to get.
   * @param documentId the id of the document to get.
   */
  public GetPublisher(final Client client, final String indexName, final String type,
      final String documentId) {
    this.client = client;
    this.indexName = indexName;
    this.type = type;
    this.documentId = documentId;
  }

  @Override
  public void subscribe(final Subscriber<? super GetResponse> subscriber) {
    final GetRequestBuilder requestBuilder =
        this.client.prepareGet(this.indexName, this.type, this.documentId).setFetchSource(true)
            .setFields("title");
    final GetSubscription subscription = new GetSubscription(subscriber, requestBuilder);
    subscriber.onSubscribe(subscription);
  }

}
