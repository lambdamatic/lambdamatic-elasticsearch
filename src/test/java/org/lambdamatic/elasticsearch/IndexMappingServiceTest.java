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
public class IndexMappingServiceTest extends BaseIntegrationTest {

  @Test
  public void shouldBuildMapping() {
    // given
    // when
    final Map<String, Object> classMapping =
        IndexMappingService.getDomainTypeMapping(Blogpost.class);
    // then
    MappingAssertions.assertThat(classMapping).hasMapping("title", "text")
        .hasMapping("content", "text").hasMapping("tags", "keyword")
        .hasMapping("publish_date", "date").hasMapping("status", "keyword")
        .hasMapping("tags", "keyword").hasMapping("comments", "object")
        .hasMapping("comments.comment", "text")
        // special field to store the corresponding domain type
        .hasMapping(DocumentCodec.DOMAIN_TYPE, "keyword")
        .hasMapping("comments." + DocumentCodec.DOMAIN_TYPE, "keyword");
  }

}
