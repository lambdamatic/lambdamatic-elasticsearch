/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.internal.elasticsearch.reactivestreams;

import java.util.concurrent.atomic.AtomicBoolean;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A <a href= "https://github.com/reactive-streams/reactive-streams-jvm/blob/v1.0.0/README.md">
 * Reactive Streams</a> {@link Subscription} a <code>SearchOperation</code> operation.
 */
public class SearchSubscription implements Subscription {

  private static final Logger LOGGER = LoggerFactory.getLogger(SearchSubscription.class);
  
  /**
   * the associated {@link Subscriber}.
   */
  final Subscriber<? super SearchResponse> subscriber;

  /**
   * The {@link GetRequestBuilder} to perform the <code>SearchOperation</code> operation.
   */
  private final SearchRequestBuilder requestBuilder;

  /** A flag to indicate if the operation was cancelled. */
  private AtomicBoolean cancelled = new AtomicBoolean(false);

  /**
   * Constructor.
   * 
   * @param subscriber the {@link Subscriber} for this {@link Subscription}.
   * @param requestBuilder the {@link SearchRequestBuilder} to perform the <code>SearchOperation</code> operation.
   */
  public SearchSubscription(final Subscriber<? super SearchResponse> subscriber,
      final SearchRequestBuilder requestBuilder) {
    this.subscriber = subscriber;
    this.requestBuilder = requestBuilder;
  }

  @Override
  public void request(final long n) {
    if (!this.cancelled.get()) {
      LOGGER.debug("Executing query...");
      this.requestBuilder.execute(new ActionListener<SearchResponse>() {

        @Override
        public void onResponse(final SearchResponse response) {
          SearchSubscription.this.subscriber.onNext(response);
          SearchSubscription.this.subscriber.onComplete();
        }

        @Override
        public void onFailure(Throwable e) {
          SearchSubscription.this.subscriber.onError(e);
        }
      });
    }
  }

  @Override
  public void cancel() {
    this.cancelled.set(false);
  }

}
