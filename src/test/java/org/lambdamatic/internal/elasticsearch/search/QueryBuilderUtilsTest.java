/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.internal.elasticsearch.search;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.elasticsearch.common.geo.builders.ShapeBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.lambdamatic.elasticsearch.querydsl.QueryExpression;
import org.lambdamatic.elasticsearch.testutils.ParametersBuilder;
import org.lambdamatic.elasticsearch.types.Location;

import com.sample.blog.QBlogpost;
import com.sample.citybikesnyc.QBikeStation;

/**
 * Testing the {@link QueryBuilderUtils} class.
 */
@RunWith(Parameterized.class)
public class QueryBuilderUtilsTest {

  /**
   * @return the test data.
   */
  @Parameters(name = "{index}")
  public static Collection<Object[]> generateData() {
    final List<Object[]> blogpostData = new ParametersBuilder<QueryExpression<QBlogpost>>()
        .add(b -> b.title.fuzzyMatches("blog"),
            Arrays.asList(QueryBuilders.fuzzyQuery("title", "blog")))
        .add(b -> b.title.fuzzyMatches("blog").boost(1.5f),
            Arrays.asList(QueryBuilders.fuzzyQuery("title", "blog").boost(1.5f)))
        .add(b -> b.comments.comment.fuzzyMatches("nice"),
            Arrays.asList(QueryBuilders.fuzzyQuery("comments.comment", "nice")))
        .add(b -> {
          b.title.fuzzyMatches("blog").boost(1.5f);
          b.comments.comment.fuzzyMatches("nice");
        }, Arrays.asList(QueryBuilders.fuzzyQuery("title", "blog").boost(1.5f),
            QueryBuilders.fuzzyQuery("comments.comment", "nice")))
        .get();
    final List<Object[]> bikestationsData = new ParametersBuilder<QueryExpression<QBikeStation>>()
        .add(b -> b.location.withinRectangle(new Location(70, 40), new Location(71, 41)),
            Arrays.asList(QueryBuilders.geoBoundingBoxQuery("location").topLeft(70, 40).bottomRight(71, 41)))
        .get();
    final List<Object[]> data = new ArrayList<>();
    data.addAll(blogpostData);
    data.addAll(bikestationsData);
    return data;
  }

  @Parameter(value = 0)
  public QueryExpression<QBlogpost> searchExpression;

  @Parameter(value = 1)
  public List<QueryBuilder> expectation;


  @Test
  public void shouldBuildQueryFromSearchExpression() {
    // when
    final List<QueryBuilder> result = QueryBuilderUtils.from(searchExpression);
    // then
    assertThat(result.toString()).isEqualTo(expectation.toString());
  }

}
