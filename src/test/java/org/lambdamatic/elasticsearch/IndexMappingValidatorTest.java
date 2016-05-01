/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.elasticsearch;

import java.io.IOException;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsResponse;
import org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsResponse.FieldMappingMetaData;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.client.Requests;
import org.elasticsearch.test.ESSingleNodeTestCase;
import org.junit.Test;
import org.lambdamatic.elasticsearch.annotations.Document;
import org.lambdamatic.elasticsearch.testutils.MappingAssertions;
import org.lambdamatic.internal.elasticsearch.IndexMappingValidator;
import org.lambdamatic.internal.elasticsearch.IndexValidationStatus;
import org.lambdamatic.internal.elasticsearch.MappingUtils;

import com.sample.blog.BlogPost;
import com.sample.blog.BlogPosts;

/**
 * Testing the {@link IndexMappingValidator}.
 */
public class IndexMappingValidatorTest extends ESSingleNodeTestCase {

  @Test
  public void shouldCreateIndex() throws IOException {
    // given
    client().admin().indices().delete(
        Requests.deleteIndexRequest(BlogPost.class.getAnnotation(Document.class).index()));
    final BlogPosts blogPosts = new BlogPosts(client());
    // when verify index, with array and list fields
    final IndexValidationStatus status = blogPosts.verifyIndex();
    // then
    Assertions.assertThat(status).isEqualTo(IndexValidationStatus.OK);
    final GetMappingsResponse fieldMappings =
        client().admin().indices().prepareGetMappings("blog_index").addTypes("blogpost").get();
    final Map<String, Object> blogPostMapping =
        fieldMappings.getMappings().get("blog_index").get("blogpost").get().sourceAsMap();
    MappingAssertions.assertThat(blogPostMapping).hasMapping("body", "string")
        .hasMapping("comments.comment", "string");
  }

  @Test
  public void shouldLocateIdFieldInDomainType() {
    // when
    final String idFieldName = MappingUtils.getIdFieldName(BlogPost.class);
    // then
    Assertions.assertThat(idFieldName).isEqualTo("id");
  }
}
