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
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.test.ESSingleNodeTestCase;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.lambdamatic.elasticsearch.testutils.Dataset;
import org.lambdamatic.elasticsearch.testutils.DatasetRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sample.blog.BlogPost;
import com.sample.blog.BlogPosts;

/**
 * Testing the Reactive Streams implementation for the <code>GET</code> operation on a single
 * Elasticsearch node.
 */
public class GetOperationTest extends ESSingleNodeTestCase {

  private static final Logger LOGGER = LoggerFactory.getLogger(GetOperationTest.class);

  private static final String BLOGPOST_INDEX_NAME = "blog_index";

  private static final String BLOGPOST_TYPE = "blogpost";

  private XContentBuilder source(String id, String nameValue) throws IOException {
    return XContentFactory.jsonBuilder().startObject().field("id", id).field("title", nameValue)
        .endObject();
  }

  @Rule
  public DatasetRule datasetRule = new DatasetRule(client());


  @Test
  @Dataset(documents = "blogposts.json")
  public void shouldGetSingleDocument() throws IOException, InterruptedException {
    // given
    final BlogPosts blogPosts = new BlogPosts(client());
    // when
    final BlogPost result = blogPosts.get("1");
    // then
    Assertions.assertThat(result.getId()).isEqualTo(1L);
    Assertions.assertThat(result.getTitle()).isEqualTo("First blog post");
  }

  @Test
  @Dataset(documents = "blogposts.json")
  public void shouldGetSingleDocumentAsync() throws IOException, InterruptedException {
    // given
    final BlogPosts blogPosts = new BlogPosts(client());
    final Queue<BlogPost> queue = new ArrayBlockingQueue<>(1);
    final CountDownLatch latch = new CountDownLatch(1);
    // when
    blogPosts.asyncGet("1", d -> {
      queue.add(d);
      latch.countDown();
    }, t -> {
      t.printStackTrace();
      latch.countDown();
    });
    // then
    latch.await(1, TimeUnit.SECONDS);
    Assertions.assertThat(queue.size()).isEqualTo(1);
    final BlogPost result = queue.poll();
    Assertions.assertThat(result.getId()).isEqualTo(1L);
    Assertions.assertThat(result.getTitle()).isEqualTo("First blog post");
  }
}
