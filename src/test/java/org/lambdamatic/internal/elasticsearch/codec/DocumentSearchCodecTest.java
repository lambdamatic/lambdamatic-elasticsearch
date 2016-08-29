/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.internal.elasticsearch.codec;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.junit.Test;
import org.lambdamatic.elasticsearch.searchdsl.QueryExpression;
import org.lambdamatic.internal.elasticsearch.searchdsl.DocumentSearch;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sample.blog.BlogpostStatus;
import com.sample.blog.QBlogpost;

/**
 * Testing the {@link DocumentSearchCodec}.
 */
public class DocumentSearchCodecTest {

  /** The usual Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(DocumentSearchCodecTest.class);

  @Test
  public void shouldSerializeDocumentSearch() throws IOException, JSONException {
    // given
    final QueryExpression<QBlogpost> mustMatchExpression =
        b -> b.title.matches("Search") && b.content.matches("Elasticsearch");
    final QueryExpression<QBlogpost> filterExpression =
        b -> b.status.hasExactTerm(BlogpostStatus.PUBLISHED)
            && b.publishDate.isGreaterOrEqualTo(LocalDate.of(2015, 1, 1));
    final DocumentSearch query =
        DocumentSearchBuilder.mustMatch(mustMatchExpression).andFilter(filterExpression).build();
    // when
    final String jsonSearchRequest = new DocumentSearchCodec().encode(query);
    // then
    final String expectation = loadExpectedContentFromFile("query-filter-context.json");
    LOGGER.debug("Comparing {} \nvs\n {}", expectation, jsonSearchRequest);
    JSONAssert.assertEquals(expectation, jsonSearchRequest, JSONCompareMode.LENIENT);
  }

  private String loadExpectedContentFromFile(final String fileName) throws IOException {
    final InputStream content = Thread.currentThread().getContextClassLoader()
        .getResourceAsStream("requests" + File.separator + fileName);
    assertThat(content).isNotNull();
    return IOUtils.toString(content);
  }

  static class DocumentSearchBuilder {

    private QueryExpression<?> shouldMatchExpression;

    private QueryExpression<?> mustMatchExpression;

    private QueryExpression<?> mustNotMatchExpression;

    private QueryExpression<?> filterExpression;

    static DocumentSearchBuilder shouldMatch(final QueryExpression<?> shouldMatchExpression) {
      final DocumentSearchBuilder documentSearchBuilder = new DocumentSearchBuilder();
      documentSearchBuilder.shouldMatchExpression = shouldMatchExpression;
      return documentSearchBuilder;
    }

    static DocumentSearchBuilder mustMatch(final QueryExpression<?> mustMatchExpression) {
      final DocumentSearchBuilder documentSearchBuilder = new DocumentSearchBuilder();
      documentSearchBuilder.mustMatchExpression = mustMatchExpression;
      return documentSearchBuilder;
    }

    DocumentSearchBuilder andMustMatch(final QueryExpression<?> mustMatchExpression) {
      this.mustMatchExpression = mustMatchExpression;
      return this;
    }

    static DocumentSearchBuilder mustNotMatch(final QueryExpression<?> mustNotMatchExpression) {
      final DocumentSearchBuilder documentSearchBuilder = new DocumentSearchBuilder();
      documentSearchBuilder.mustNotMatchExpression = mustNotMatchExpression;
      return documentSearchBuilder;
    }

    DocumentSearchBuilder andMustNotMatch(final QueryExpression<?> mustNotMatchExpression) {
      this.mustNotMatchExpression = mustNotMatchExpression;
      return this;
    }

    static DocumentSearchBuilder filter(final QueryExpression<?> filterExpression) {
      final DocumentSearchBuilder documentSearchBuilder = new DocumentSearchBuilder();
      documentSearchBuilder.filterExpression = filterExpression;
      return documentSearchBuilder;
    }

    DocumentSearchBuilder andFilter(final QueryExpression<?> filterExpression) {
      this.filterExpression = filterExpression;
      return this;
    }

    DocumentSearch build() {
      return new DocumentSearch(shouldMatchExpression, mustMatchExpression, mustNotMatchExpression,
          filterExpression);
    }

  }
}
