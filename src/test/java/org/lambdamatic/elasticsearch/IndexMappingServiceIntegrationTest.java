/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.elasticsearch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.lambdamatic.elasticsearch.annotations.Document;
import org.lambdamatic.elasticsearch.exceptions.ClientResponseException;
import org.lambdamatic.internal.elasticsearch.IndexMappingService;
import org.lambdamatic.internal.elasticsearch.IndexValidationStatus;
import org.lambdamatic.internal.elasticsearch.clientdsl.responses.GetIndexMappingsResponse;
import org.lambdamatic.internal.elasticsearch.clientdsl.responses.GetIndexMappingsResponse.TypeMapping;
import org.lambdamatic.internal.elasticsearch.codec.DocumentCodec;
import org.lambdamatic.internal.elasticsearch.testutils.MappingAssertions;
import org.lambdamatic.internal.elasticsearch.testutils.TypeMappingAssertions;

import com.sample.blog.Blogpost;
import com.sample.blog.Blogposts;

/**
 * Testing the {@link IndexMappingService}.
 */
public class IndexMappingServiceIntegrationTest extends BaseIntegrationTest {

  @Test
  public void shouldCreateIndex() throws IOException {
    // given
    try {
      client().deleteIndex(Blogpost.class.getAnnotation(Document.class).index());
    } catch (ClientResponseException e) {
      if (e.getErrorResponse().getStatus() == 404) {
        // ignore, index just did not exist
      } else {
        fail("Failed to delete index: " + e.getErrorResponse().getError().getReason());
      }
    }
    final Blogposts blogPosts = new Blogposts(client());
    // when verify index, with array and list fields
    final IndexValidationStatus status = blogPosts.verifyIndex();
    // then
    Assertions.assertThat(status).isEqualTo(IndexValidationStatus.OK);
    final GetIndexMappingsResponse indexMappings =
        client().getIndexMappings(Blogposts.BLOGPOST_INDEX_NAME, Blogposts.BLOGPOST_TYPE);
    final TypeMapping blogPostTypeMapping = indexMappings
        .getIndexMappings(Blogposts.BLOGPOST_INDEX_NAME).getTypeMapping(Blogposts.BLOGPOST_TYPE);
    assertThat(blogPostTypeMapping).isNotNull();
    TypeMappingAssertions.assertThat(blogPostTypeMapping).isNotNull().hasMapping("title", "text")
        .hasMapping("content", "text").hasMapping("tags", "keyword")
        .hasMapping("publish_date", "date").hasMapping("status", "keyword")
        .hasMapping("tags", "keyword")
        .hasMapping("comments.comment", "text")
        // special field to store the corresponding domain type
        .hasMapping(DocumentCodec.DOMAIN_TYPE, "keyword")
        .hasMapping("comments." + DocumentCodec.DOMAIN_TYPE, "keyword");
  }

}
