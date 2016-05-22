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
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * A <a href= "https://github.com/reactive-streams/reactive-streams-jvm/blob/v1.0.0/README.md">
 * Reactive Streams</a> {@link Subscription} for an <code>Index</code> operation.
 */
public class IndexSubscription implements Subscription {

  /**
   * the associated {@link Subscriber}.
   */
  final Subscriber<? super IndexResponse> subscriber;

  /**
   * The {@link IndexRequestBuilder} to perform the <code>Index</code> operation.
   */
  private final IndexRequestBuilder requestBuilder;

  /** A flag to indicate if the operation was cancelled. */
  private AtomicBoolean cancelled = new AtomicBoolean(false);

  /**
   * Constructor.
   * 
   * @param subscriber the {@link Subscriber} for this {@link Subscription}.
   * @param requestBuilder the {@link IndexRequestBuilder} to perform the <code>Index</code> operation.
   */
  public IndexSubscription(final Subscriber<? super IndexResponse> subscriber,
      final IndexRequestBuilder requestBuilder) {
    this.subscriber = subscriber;
    this.requestBuilder = requestBuilder;
  }

  @Override
  public void request(final long n) {
    if (!this.cancelled.get()) {
      this.requestBuilder
          .execute(new ActionListener<IndexResponse>() {

            @Override
            public void onResponse(final IndexResponse response) {
              IndexSubscription.this.subscriber.onNext(response);
              IndexSubscription.this.subscriber.onComplete();
            }

            @Override
            public void onFailure(Throwable e) {
              IndexSubscription.this.subscriber.onError(e);
            }
          });
    }
  }

  @Override
  public void cancel() {
    this.cancelled.set(false);
  }

}
