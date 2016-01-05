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
import org.elasticsearch.action.get.GetResponse;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * A <a href= "https://github.com/reactive-streams/reactive-streams-jvm/blob/v1.0.0/README.md">
 * Reactive Streams</a> {@link Subscription} a <code>GET</code> operation.
 */
public class GetSubscription implements Subscription {

  /**
   * the associated {@link Subscriber}.
   */
  final Subscriber<? super GetResponse> subscriber;

  /**
   * The {@link GetRequestBuilder} to perform the <code>GET</code> operation.
   */
  private final GetRequestBuilder requestBuilder;

  /** A flag to indicate if the operation was cancelled. */
  private AtomicBoolean cancelled = new AtomicBoolean(false);

  /**
   * Constructor.
   * 
   * @param subscriber the {@link Subscriber} for this {@link Subscription}.
   * @param requestBuilder the {@link GetRequestBuilder} to perform the <code>GET</code> operation.
   */
  public GetSubscription(final Subscriber<? super GetResponse> subscriber,
      final GetRequestBuilder requestBuilder) {
    this.subscriber = subscriber;
    this.requestBuilder = requestBuilder;
  }

  @Override
  public void request(final long n) {
    if (!this.cancelled.get()) {
      this.requestBuilder.setFetchSource(true).setFields("title")
          .execute(new ActionListener<GetResponse>() {

            @Override
            public void onResponse(final GetResponse response) {
              GetSubscription.this.subscriber.onNext(response);
              GetSubscription.this.subscriber.onComplete();
            }

            @Override
            public void onFailure(Throwable e) {
              GetSubscription.this.subscriber.onError(e);
            }
          });
    }

  }

  @Override
  public void cancel() {
    this.cancelled.set(false);
  }

}
