/*******************************************************************************
 * Copyright (c) 2016 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.internal.elasticsearch.search;

import static org.assertj.core.api.Assertions.assertThat;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;
import org.lambdamatic.elasticsearch.search.SearchExpression;

import com.sample.QBlogPost;

/**
 * Testing the {@link QueryBuilderUtils} class.
 */
public class QueryBuilderUtilsTest {

  @Test
  public void shouldBuildQueryFromSearchExpression() {
    // given
    final SearchExpression<QBlogPost> searchExpression = b -> b.title.similarTo("blog");
    // when
    final QueryBuilder result = QueryBuilderUtils.from(searchExpression);
    // then
    final QueryBuilder expected = QueryBuilders.fuzzyQuery("title", "blog");
    assertThat(result.toString()).isNotNull().isEqualTo(expected.toString());
  }
}
