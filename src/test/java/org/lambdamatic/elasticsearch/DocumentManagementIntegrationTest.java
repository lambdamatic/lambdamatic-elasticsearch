/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.elasticsearch;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.lambdamatic.internal.elasticsearch.testutils.Dataset;
import org.lambdamatic.internal.elasticsearch.testutils.DatasetRule;
import org.lambdamatic.internal.elasticsearch.testutils.ESAssertions;
import org.lambdamatic.internal.elasticsearch.testutils.TestWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sample.blog.Blogpost;
import com.sample.blog.Blogposts;

/**
 * Testing the Reactive Streams implementation for the <code>GET</code> operation on a single
 * Elasticsearch node.
 */
public class DocumentManagementIntegrationTest extends BaseIntegrationTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(DocumentManagementIntegrationTest.class);

  @Rule
  public DatasetRule datasetRule = new DatasetRule(client());

  @Rule
  public TestWatcher testWatcher = new TestWatcher();
  
  @Test
  @Dataset(settings = "blogposts-settings.json", documents = "blogposts-data.json")
  public void shouldGetSingleDocument() throws IOException, InterruptedException {
    // given
    final Blogposts blogPosts = new Blogposts(client());
    // when
    final Blogpost result = blogPosts.get("1");
    // then
    Assertions.assertThat(result.getId()).isEqualTo(1L);
    Assertions.assertThat(result.getTitle()).isEqualTo("First blog post");
  }

  @Test
  @Dataset(settings = "blogposts-settings.json", documents = "blogposts-data.json")
  public void shouldGetSingleDocumentAsync() throws IOException, InterruptedException {
    // given
    final Blogposts blogPosts = new Blogposts(client());
    final Queue<Blogpost> queue = new ArrayBlockingQueue<>(1);
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
    final Blogpost result = queue.poll();
    Assertions.assertThat(result.getId()).isEqualTo(1L);
    Assertions.assertThat(result.getTitle()).isEqualTo("First blog post");
  }

  @Test
  @Dataset(settings = "blogposts-settings.json")
  public void shouldIndexSimpleBlogpostWithId() throws InterruptedException {
    // given
    final Blogposts blogPosts = new Blogposts(client());
    final Blogpost blogpost = new Blogpost();
    blogpost.setId(1L);
    blogpost.setTitle("Title ipsum");
    blogpost.setContent("Lorem ipsum");
    // when
    blogPosts.index(blogpost);
    // then
    // blogpost#id must not change
    Assertions.assertThat(blogpost.getId()).isEqualTo(1L);
    assertTrue(client().getDocument(Blogposts.BLOGPOST_INDEX_NAME, Blogposts.BLOGPOST_TYPE, "1").isExists());
    ESAssertions.assertThat(client()).hasIndexSize(Blogposts.BLOGPOST_INDEX_NAME, 1);
  }

  @Test
  @Ignore
  // does not work because Blogpost#id is a 'long' and ES will generate a random ID of type 'String'
  // if none is provided.
  public void shouldIndexSimpleBlogpostWithoutId() {
    // given
    final Blogposts blogPosts = new Blogposts(client());
    final Blogpost blogpost = new Blogpost();
    blogpost.setTitle("Title ipsum");
    blogpost.setContent("Lorem ipsum");
    // when
    blogPosts.index(blogpost);
    // then
    ESAssertions.assertThat(client()).hasIndexSize(Blogposts.BLOGPOST_INDEX_NAME, 1);
    Assertions.assertThat(blogpost.getId()).isNotNull();
  }

  @Test
  @Dataset(settings = "blogposts-settings.json")
  public void shouldIndexSimpleBlogpostAsync() throws InterruptedException {
    // given
    final Blogposts blogPosts = new Blogposts(client());
    final Blogpost blogpost = new Blogpost();
    blogpost.setId(1L);
    blogpost.setTitle("Title ipsum");
    blogpost.setContent("Lorem ipsum");
    final CountDownLatch latch = new CountDownLatch(1);
    // when
    blogPosts.asyncIndex(blogpost, d -> {
      latch.countDown();
    }, t -> {
      latch.countDown();
    });

    // then
    latch.await(1, TimeUnit.SECONDS);
    // blogpost#id must not change
    Assertions.assertThat(blogpost.getId()).isEqualTo(1L);
    assertTrue(client().getDocument(Blogposts.BLOGPOST_INDEX_NAME, Blogposts.BLOGPOST_TYPE, "1").isExists());
    // give 1s to index the document
    Thread.sleep(TimeUnit.SECONDS.toMillis(1));
    ESAssertions.assertThat(client()).hasIndexSize(Blogposts.BLOGPOST_INDEX_NAME, 1);
  }
}
