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
import org.elasticsearch.test.ESSingleNodeTestCase;
import org.junit.Rule;
import org.junit.Test;
import org.lambdamatic.elasticsearch.testutils.Dataset;
import org.lambdamatic.elasticsearch.testutils.DatasetRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sample.blog.BlogPost;
import com.sample.blog.BlogPosts;
import com.sample.blog.Comment;

/**
 * Testing the Reactive Streams implementation for the <code>SEARCH</code> operation on a single
 * Elasticsearch node.
 */
public class SearchOperationTest extends ESSingleNodeTestCase {

  private static final Logger LOGGER = LoggerFactory.getLogger(SearchOperationTest.class);

  @Rule
  public DatasetRule datasetRule = new DatasetRule(client());

  private BlogPost firstBlogPost() {
    final BlogPost firstBlogPost = new BlogPost();
    firstBlogPost.setId(1L);
    firstBlogPost.setTitle("First blog post");
    return firstBlogPost;
  }

  private BlogPost secondBlogPost() {
    final BlogPost secondBlogPost = new BlogPost();
    secondBlogPost.setId(2L);
    secondBlogPost.setTitle("Second blog post");
    secondBlogPost.setComments(
        Arrays.asList(new Comment("Xavier", "Nice work!", 5, LocalDate.of(2016, Month.APRIL, 1))));
    return secondBlogPost;
  }

  @Test
  @Dataset(settings = "settings.json", documents = "blogposts.json")
  public void filterSingleDocumentByTitle() throws IOException, InterruptedException {
    // given
    final BlogPosts blogPosts = new BlogPosts(client());
    // when
    final List<BlogPost> result =
        blogPosts.filter(p -> p.title.fuzzyMatches("post")).collect(Collectors.toList());
    // then
    Assertions.assertThat(result.size()).isEqualTo(2);
    Assertions.assertThat(result).contains(firstBlogPost(), secondBlogPost());
  }

  @Test
  @Dataset(settings = "settings.json", documents = "blogposts.json")
  public void shouldMatchSingleDocumentByComment() throws IOException, InterruptedException {
    // given
    final BlogPosts blogPosts = new BlogPosts(client());
    // when
    final List<BlogPost> result =
        blogPosts.shouldMatch(p -> p.comments.comment.matches("nice")).collect(Collectors.toList());
    // then
    Assertions.assertThat(result.size()).isEqualTo(1);
    Assertions.assertThat(result).contains(secondBlogPost());
  }

  @Test
  @Dataset(settings = "settings.json", documents = "blogposts.json")
  public void mustMatchSingleDocumentByComment() throws IOException, InterruptedException {
    // given
    final BlogPosts blogPosts = new BlogPosts(client());
    // when
    final List<BlogPost> result =
        blogPosts.mustMatch(p -> p.comments.comment.matches("nice")).collect(Collectors.toList());
    // then
    Assertions.assertThat(result.size()).isEqualTo(1);
    Assertions.assertThat(result).contains(secondBlogPost());
  }

  @Test
  @Dataset(settings = "settings.json", documents = "blogposts.json")
  public void filterSingleDocumentByComment() throws IOException, InterruptedException {
    // given
    final BlogPosts blogPosts = new BlogPosts(client());
    // when
    final List<BlogPost> result =
        blogPosts.filter(p -> p.comments.comment.matches("nice")).collect(Collectors.toList());
    // then
    Assertions.assertThat(result.size()).isEqualTo(1);
    Assertions.assertThat(result).contains(secondBlogPost());
  }

  @Test
  @Dataset(settings = "settings.json", documents = "blogposts.json")
  public void queryAndFilterSingleDocument() throws IOException, InterruptedException {
    // given
    final BlogPosts blogPosts = new BlogPosts(client());
    // when
    final List<BlogPost> result = blogPosts.shouldMatch(p -> p.title.fuzzyMatches("second")) 
        .filter(p -> p.comments.comment.matches("nice")).collect(Collectors.toList());
    // then
    Assertions.assertThat(result.size()).isEqualTo(1);
    Assertions.assertThat(result).contains(secondBlogPost());
  }

  @Test
  @Dataset(settings = "settings.json", documents = "blogposts.json")
  public void queryAndFilterAllDocument() throws IOException, InterruptedException {
    // given
    final BlogPosts blogPosts = new BlogPosts(client());
    // when
    final List<BlogPost> result = blogPosts
        //.shouldMatch(p -> p.title.fuzzyMatches("second")) 
        .filter(p -> p.title.matches("post"))
        .collect(Collectors.toList());
    // then
    Assertions.assertThat(result.size()).isEqualTo(2);
    Assertions.assertThat(result).contains(firstBlogPost());
    Assertions.assertThat(result).contains(secondBlogPost());
  }



}
