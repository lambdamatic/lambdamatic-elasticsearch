/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.internal.elasticsearch.reactivestreams;

import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A <a href= "https://github.com/reactive-streams/reactive-streams-jvm/blob/v1.0.0/README.md">
 * Reactive Streams</a> {@link Subscription} for a <code>SearchOperation</code> operation.
 * 
 * @param <D> the domain type that is searched
 */
public class SearchSubscription<D> implements Subscription {

  static final Logger LOGGER = LoggerFactory.getLogger(SearchSubscription.class);

  /**
   * the associated {@link Subscriber}.
   */
  final Subscriber<SearchHit> subscriber;

  /**
   * {@link Iterator} of {@link SearchHits} waiting to be transmitted to the {@link Subscriber}.
   */
  Iterator<SearchHit> searchHitsIterator = null;

  /**
   * The {@link GetRequestBuilder} to perform the <code>SearchOperation</code> operation.
   */
  private final SearchRequestBuilder requestBuilder;

  /** A flag to indicate if the operation was cancelled. */
  private AtomicBoolean cancelled = new AtomicBoolean(false);

  /** A flag to indicate if the operation was cancelled. */
  private AtomicBoolean executed = new AtomicBoolean(false);

  /**
   * Constructor.
   * 
   * @param subscriber the {@link Subscriber} for this {@link Subscription}.
   * @param requestBuilder the {@link SearchRequestBuilder} to perform the
   *        <code>SearchOperation</code> operation.
   */
  public SearchSubscription(final Subscriber<SearchHit> subscriber,
      final SearchRequestBuilder requestBuilder) {
    this.subscriber = subscriber;
    this.requestBuilder = requestBuilder;
  }

  @Override
  public void request(final long n) {
    // on first call, the request must be executed
    if (!this.executed.get()) {
      LOGGER.debug("Executing query...");
      final CountDownLatch latch = new CountDownLatch(1);
      this.requestBuilder.execute(new ActionListener<SearchResponse>() {

        /**
         * Collect the SearchResponse, retrieves the hits, converts into domain types and passes to
         * the Subscriber.
         */
        @Override
        public void onResponse(final SearchResponse response) {
          LOGGER.debug("Search response: \n{}", response);
          SearchSubscription.this.searchHitsIterator = response.getHits().iterator();
        }

        @Override
        public void onFailure(Throwable e) {
          SearchSubscription.this.subscriber.onError(e);
        }
      });
      try {
        latch.await();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      } finally {
        this.executed.set(true);
      }
    }
    if (!this.cancelled.get()) {
      // check if there's a SearchHit to return to the Subscriber
      if (this.searchHitsIterator != null && this.searchHitsIterator.hasNext()) {
        SearchSubscription.this.subscriber.onNext(this.searchHitsIterator.next());
      } else {
        SearchSubscription.this.subscriber.onComplete();
      }

    }
  }

  @Override
  public void cancel() {
    this.cancelled.set(false);
  }

}
