/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.internal.elasticsearch.codec;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.lambdamatic.internal.elasticsearch.MappingException;

import com.sample.blog.Blogpost;
import com.sample.blog.Comment;

/**
 * Testing the {@link DocumentCodec} class.
 */
public class DocumentCodecTest {

  private DocumentCodec<Blogpost> documentCodec;

  @Before
  public void setup() {
    this.documentCodec = new DocumentCodec<>(Blogpost.class);
  }

  @Test
  public void shouldConvertSourceMapToSimpleBlogPost() {
    // given
    final String documentId = "1";
    final Map<String, Object> documentSource = new HashMap<>();
    documentSource.put("title", "example");
    // when
    final Blogpost result = this.documentCodec.toDomainType(documentId, documentSource);
    // then
    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getTitle()).isEqualTo("example");
  }

  @Test
  public void shouldConvertSimpleBlogPostToSourceMap() {
    // given
    final Blogpost blogpost = new Blogpost();
    blogpost.setId(1L);
    blogpost.setTitle("example");
    // when
    final Map<String, Object> sourceMap = this.documentCodec.toSourceMap(blogpost);
    // then id should not be part of the sourceMap, but other fields, yes.
    assertThat(sourceMap.get("id")).isNull();
    assertThat(sourceMap.get("title")).isEqualTo("example");
  }

  @Test
  public void shouldConvertBlogPostWithCommentsToSourceMap() {
    // given
    final Blogpost blogpost = new Blogpost();
    blogpost.setId(1L);
    blogpost.setTitle("example");
    LocalDate now = LocalDate.now();
    final Comment firstComment = new Comment("author1", "comment1", 1, now);
    final Comment secondComment = new Comment("author2", "comment2", 2, now);
    blogpost.setComments(Arrays.asList(firstComment, secondComment));
    // when
    final Map<String, Object> sourceMap = this.documentCodec.toSourceMap(blogpost);
    // then id should not be part of the sourceMap, but other fields, yes.
    assertThat(sourceMap.get("id")).isNull();
    assertThat(sourceMap.get("title")).isEqualTo("example");
    assertThat(sourceMap.get("comments")).isInstanceOf(List.class);
    final List<Object> commentsSourceMap = (List<Object>)sourceMap.get("comments");
    assertThat(commentsSourceMap).hasSize(2);
    final Map<String, Object> firstCommentSourceMap = (Map<String, Object>)(commentsSourceMap.get(0));
    assertThat(firstCommentSourceMap).containsEntry("authorName", "author1");
    assertThat(firstCommentSourceMap).containsEntry("comment", "comment1");
    assertThat(firstCommentSourceMap).containsEntry("stars", 1);
    assertThat(firstCommentSourceMap).containsEntry("date", now);
    final Map<String, Object> secondCommentSourceMap = (Map<String, Object>)(commentsSourceMap.get(1));
    assertThat(secondCommentSourceMap).containsEntry("authorName", "author2");
    assertThat(secondCommentSourceMap).containsEntry("comment", "comment2");
    assertThat(secondCommentSourceMap).containsEntry("stars", 2);
    assertThat(secondCommentSourceMap).containsEntry("date", now);
  }

  @Test
  public void shouldFindIdField() {
    // when
    final String idFieldName = DocumentCodec.getIdField(Blogpost.class).getName();
    // then
    Assertions.assertThat(idFieldName).isEqualTo("id");
  }

  @Ignore
  @Test(expected = MappingException.class)
  public void shouldFailWhenNoIdFieldExists() {
    // when
    DocumentCodec.getIdField(Comment.class).getName();
    // expect an exception
  }

  @Ignore
  @Test(expected = MappingException.class)
  public void shouldFailWhenTooManyIdFieldsExist() {
    fail("Not implemented yet");
  }

}
