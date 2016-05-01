/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.internal.elasticsearch.search;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.lambdamatic.internal.elasticsearch.search.streams.Converter;

import com.sample.blog.BlogPost;
import com.sample.blog.Comment;

/**
 * Testing the {@link Converter} utility class
 */
public class ConverterTest {

  @Test
  public void shouldConvertSimpleBlogPost() {
    // given
    final String documentId = "1";
    final Map<String, Object> documentSource = new HashMap<>();
    documentSource.put("title", "example");
    // when
    final BlogPost result = Converter.toDomainType(BlogPost.class, documentId, documentSource);
    // then
    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getTitle()).isEqualTo("example");
  }

  @Test
  public void shouldConvertBlogPostWithComments() {
    // given
    final String documentId = "1";
    final Map<String, Object> documentSource = new HashMap<>();
    final Map<String, Object> commentSource = new HashMap<>();
    commentSource.put("authorName", "Xavier");
    commentSource.put("comment", "Nice work!");
    commentSource.put("date", "2016-04-01");
    commentSource.put("stars", 5);
    documentSource.put("comments", Arrays.asList(commentSource));
    // when
    final BlogPost result = Converter.toDomainType(BlogPost.class, documentId, documentSource);
    // then
    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getComments()).hasSize(1)
        .contains(new Comment("Xavier", "Nice work!", 5, LocalDate.of(2016, Month.APRIL, 1)));
  }
}
