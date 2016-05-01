/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.internal.elasticsearch.search;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.lambdamatic.elasticsearch.querydsl.QueryExpression;
import org.lambdamatic.elasticsearch.testutils.ParametersBuilder;

import com.sample.blog.QBlogPost;

/**
 * Testing the {@link QueryBuilderUtils} class.
 */
@RunWith(Parameterized.class)
public class QueryBuilderUtilsTest {

  @Parameters(name = "{index}")
  public static Collection<Object[]> generateData() {
    return new ParametersBuilder<QueryExpression<QBlogPost>>()
        .add(b -> b.title.fuzzyMatches("blog"), QueryBuilders.fuzzyQuery("title", "blog"))
        .add(b -> b.comments.comment.fuzzyMatches("nice"),
            QueryBuilders.fuzzyQuery("comments.comment", "nice"))
        .get();
  }

  @Parameter(value = 0)
  public QueryExpression<QBlogPost> searchExpression;

  @Parameter(value = 1)
  public QueryBuilder expectation;


  @Test
  public void shouldBuildQueryFromSearchExpression() {
    // when
    final QueryBuilder result = QueryBuilderUtils.from(searchExpression);
    // then
    assertThat(result.toString()).isNotNull().isEqualTo(expectation.toString());
  }

}
