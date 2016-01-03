/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.elasticsearch;

import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.IndexService;
import org.elasticsearch.test.ESSingleNodeTestCase;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import com.sample.BlogPostIndex;

/**
 * Testing the Reactive Streams implementation for the <code>GET</code> operation on a single
 * Elasticsearch node.
 */
public class GetOperationTest extends ESSingleNodeTestCase {

  private static final String BLOGPOST_INDEX_NAME = "blog_index";
  private static final String BLOGPOST_TYPE = "blogpost";

  private XContentBuilder source(String id, String nameValue) throws IOException {
    return XContentFactory.jsonBuilder().startObject().field("id", id).field("title", nameValue)
        .endObject();
  }

  @Test
  public void shouldGetSingleDocument() throws IOException, InterruptedException {
    // given
    final IndexService indexService = createIndex(BLOGPOST_INDEX_NAME);
    final BulkResponse bulkResponse = client().prepareBulk()
        .add(client().prepareIndex().setIndex(BLOGPOST_INDEX_NAME).setType(BLOGPOST_TYPE).setId("1")
            .setSource(source("1", "title1")))
        .add(client().prepareIndex().setIndex(BLOGPOST_INDEX_NAME).setType(BLOGPOST_TYPE).setId("2")
            .setSource(source("2", "title2")))
        .add(client().prepareIndex().setIndex(BLOGPOST_INDEX_NAME).setType(BLOGPOST_TYPE)
            .setSource(source("3", "title3")))
        .execute().actionGet();
    assertThat(bulkResponse.getItems().length, equalTo(3));
    assertThat(bulkResponse.hasFailures(), equalTo(false));
    // when
    final BlogPostIndex blogPostIndex = new BlogPostIndex(client());
    final Queue<GetResponse> queue = new ArrayBlockingQueue<>(1);
    final CountDownLatch latch = new CountDownLatch(1);
    blogPostIndex.get("1").subscribe(new Subscriber<GetResponse>() {

      @Override
      public void onSubscribe(Subscription s) {
        s.request(1);
      }

      @Override
      public void onNext(GetResponse t) {
        queue.add(t);
        t.forEach(System.err::println);
      }

      @Override
      public void onError(Throwable t) {
        Assertions.fail("Failed to retrieve element", t);
        
      }

      @Override
      public void onComplete() {
        latch.countDown();
        
      }});
    // then
    latch.await(1, TimeUnit.SECONDS);
    assertThat(queue.size(), equalTo(1));
  }
}
