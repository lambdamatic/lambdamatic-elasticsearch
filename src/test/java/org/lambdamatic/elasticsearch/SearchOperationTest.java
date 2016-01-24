/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.elasticsearch;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.test.ESSingleNodeTestCase;
import org.junit.Rule;
import org.junit.Test;
import org.lambdamatic.elasticsearch.testutils.Dataset;
import org.lambdamatic.elasticsearch.testutils.DatasetRule;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sample.BlogPosts;

/**
 * Testing the Reactive Streams implementation for the <code>SEARCH</code> operation on a single
 * Elasticsearch node.
 */
public class SearchOperationTest extends ESSingleNodeTestCase {

  private static final Logger LOGGER = LoggerFactory.getLogger(SearchOperationTest.class);

  private static final String BLOGPOST_INDEX_NAME = "blog_index";

  private static final String BLOGPOST_TYPE = "blogpost";

  @Rule
  public DatasetRule datasetRule = new DatasetRule(client());


  @Test
  @Dataset(settings = "settings.json", documents = "blogposts.json")
  public void shouldFindSingleDocument() throws IOException, InterruptedException {
    // given
    final BlogPosts blogPosts = new BlogPosts(client());
    final Queue<SearchResponse> queue = new ArrayBlockingQueue<>(1);
    final CountDownLatch latch = new CountDownLatch(1);

    // when
    blogPosts.search(p -> p.title.similarTo("post")).subscribe(new Subscriber<SearchResponse>() {

      @Override
      public void onSubscribe(Subscription s) {
        s.request(1);
      }

      @Override
      public void onNext(SearchResponse response) {
        queue.add(response);
        LOGGER.info(response.toString());
      }

      @Override
      public void onError(Throwable t) {
        Assertions.fail("Failed to retrieve next element", t);
        latch.countDown();
      }

      @Override
      public void onComplete() {
        latch.countDown();
      }

    });
    // then
    latch.await();
    Assertions.assertThat(queue.size()).isEqualTo(1);
    final SearchResponse response = queue.poll();
    Assertions.assertThat(response.getHits().getTotalHits()).isEqualTo(1);
  }
}
