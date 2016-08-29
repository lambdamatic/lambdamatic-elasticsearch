/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.elasticsearch;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.lambdamatic.elasticsearch.types.Location;
import org.lambdamatic.internal.elasticsearch.testutils.Dataset;
import org.lambdamatic.internal.elasticsearch.testutils.DatasetRule;
import org.lambdamatic.internal.elasticsearch.testutils.TestWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sample.blog.Blogpost;
import com.sample.blog.BlogpostStatus;
import com.sample.blog.Blogposts;
import com.sample.blog.Comment;
import com.sample.citybikesnyc.BikeStation;
import com.sample.citybikesnyc.BikeStations;

/**
 * Testing the Reactive Streams implementation for the <code>SEARCH</code> operation on a single
 * Elasticsearch node.
 */
@Dataset(settings = "blogposts-settings.json", documents = "blogposts-data.json")
@Dataset(settings = "bikestations-settings.json", documents = "bikestations-data.json")
public class DocumentSearchIntegrationTest extends BaseIntegrationTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(DocumentSearchIntegrationTest.class);

  @ClassRule
  public static DatasetRule datasetRule = new DatasetRule(client());

  @Rule
  public TestWatcher testWatcher = new TestWatcher();

  private Blogpost firstBlogPost() {
    final Blogpost firstBlogPost = new Blogpost();
    firstBlogPost.setId(1L);
    firstBlogPost.setTitle("First blog post");
    return firstBlogPost;
  }

  private Blogpost secondBlogPost() {
    final Blogpost secondBlogPost = new Blogpost();
    secondBlogPost.setId(2L);
    secondBlogPost.setTitle("Second blog post");
    secondBlogPost.setStatus(BlogpostStatus.PUBLISHED);
    secondBlogPost.setComments(
        Arrays.asList(new Comment("Xavier", "Nice work!", 5, LocalDate.of(2016, Month.APRIL, 1))));
    return secondBlogPost;
  }

  @Test
  public void filterSingleDocumentByTitle() throws IOException, InterruptedException {
    // given
    final Blogposts blogPosts = new Blogposts(client());
    // when
    final List<Blogpost> result =
        blogPosts.filter(p -> p.title.matches("post")).collect(Collectors.toList());
    // then
    Assertions.assertThat(result.size()).isEqualTo(2);
    Assertions.assertThat(result).contains(firstBlogPost(), secondBlogPost());
  }

  @Test
  public void shouldMatchSingleDocumentByComment() throws IOException, InterruptedException {
    // given
    final Blogposts blogPosts = new Blogposts(client());
    // when
    final List<Blogpost> result =
        blogPosts.filter(p -> p.comments.comment.matches("nice")).collect(Collectors.toList());
    // then
    Assertions.assertThat(result.size()).isEqualTo(1);
    Assertions.assertThat(result).contains(secondBlogPost());
  }

  @Test
  public void mustMatchSingleDocumentByComment() throws IOException, InterruptedException {
    // given
    final Blogposts blogPosts = new Blogposts(client());
    // when
    final List<Blogpost> result =
        blogPosts.filter(p -> p.comments.comment.matches("nice")).collect(Collectors.toList());
    // then
    Assertions.assertThat(result.size()).isEqualTo(1);
    Assertions.assertThat(result).contains(secondBlogPost());
  }

  @Test
  public void filterSingleDocumentByComment() throws IOException, InterruptedException {
    // given
    final Blogposts blogPosts = new Blogposts(client());
    // when
    final List<Blogpost> result =
        blogPosts.filter(p -> p.comments.comment.matches("nice")).collect(Collectors.toList());
    // then
    Assertions.assertThat(result.size()).isEqualTo(1);
    Assertions.assertThat(result).contains(secondBlogPost());
  }

  @Test
  public void queryAndFilterSingleDocument() throws IOException, InterruptedException {
    // given
    final Blogposts blogPosts = new Blogposts(client());
    // when
    final List<Blogpost> result = blogPosts.shouldMatch(p -> p.title.matches("second"))
        .filter(p -> p.status.hasExactTerm(BlogpostStatus.PUBLISHED)).collect(Collectors.toList());
    // then
    Assertions.assertThat(result.size()).isEqualTo(1);
    Assertions.assertThat(result).contains(secondBlogPost());
  }

  @Test
  public void queryAndFilterAllDocument() throws IOException, InterruptedException {
    // given
    final Blogposts blogPosts = new Blogposts(client());
    // when
    final List<Blogpost> result = blogPosts.shouldMatch(p -> p.title.matches("second"))
        .filter(p -> p.title.matches("post")).collect(Collectors.toList());
    // then
    Assertions.assertThat(result.size()).isEqualTo(2);
    Assertions.assertThat(result).contains(firstBlogPost());
    Assertions.assertThat(result).contains(secondBlogPost());
  }

  @Test
  public void findWithinRectangle() {
    // given
    final BikeStations bikestations = new BikeStations(client());
    // when
    final List<BikeStation> result = bikestations
        .filter(p -> p.location.withinRectangle(new Location(42, -72), new Location(40, -74)))
        .collect(Collectors.toList());
    // then
    Assertions.assertThat(result.size()).isEqualTo(1);
  }

}
