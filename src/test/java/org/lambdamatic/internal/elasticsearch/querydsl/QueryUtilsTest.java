/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.internal.elasticsearch.querydsl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.lambdamatic.elasticsearch.searchdsl.QueryExpression;
import org.lambdamatic.elasticsearch.searchdsl.types.Boost;
import org.lambdamatic.elasticsearch.types.Location;
import org.lambdamatic.internal.elasticsearch.searchdsl.BooleanQuery;
import org.lambdamatic.internal.elasticsearch.searchdsl.GeoBoundingBoxQuery;
import org.lambdamatic.internal.elasticsearch.searchdsl.MatchQuery;
import org.lambdamatic.internal.elasticsearch.searchdsl.Query;
import org.lambdamatic.internal.elasticsearch.searchdsl.QueryUtils;
import org.lambdamatic.internal.elasticsearch.testutils.ParametersBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sample.blog.QBlogpost;
import com.sample.citybikesnyc.QBikeStation;

/**
 * Testing the {@link QueryBuilder} class.
 * 
 * @see <a href=
 *      "https://www.elastic.co/blog/lost-in-translation-boolean-operations-and-filters-in-the-bool-query">Lost
 *      in Translation: Boolean Operations and Filters in the Bool Query</a>
 */
@RunWith(Parameterized.class)
public class QueryUtilsTest {

  /** The usual Logger.*/
  private static final Logger LOGGER = LoggerFactory.getLogger(QueryUtilsTest.class);
  
  /**
   * @return the test data.
   */
  @Parameters(name = "{1}")
  public static Collection<Object[]> generateData() {
    final List<Object[]> blogpostData = new ParametersBuilder<QBlogpost>()
        .add(b ->  b.title.matches("blog"), new MatchQuery("title", "blog"))
        .add(b -> b.title.matching("blog").boost(1.5f), new MatchQuery("title", "blog").boost(1.5f))
        .add(b -> b.comments.comment.matches("nice"), new MatchQuery("comments.comment", "nice"))
        .add(b -> b.title.matching("blog").boost(1.5f) && b.comments.comment.matches("nice"),
            BooleanQuery.and(new MatchQuery("title", "blog").boost(1.5f),
                new MatchQuery("comments.comment", "nice")))
        .get();
    final List<Object[]> bikestationsData =
        new ParametersBuilder<QBikeStation>()
            .add(b -> b.location.withinRectangle(new Location(70, 40), new Location(71, 41)),
                GeoBoundingBoxQuery.fieldName("location").topLeft(70, 40).bottomRight(71, 41))
            .get();
    final List<Object[]> data = new ArrayList<>();
    data.addAll(blogpostData);
    data.addAll(bikestationsData);
    return data;
  }

  @Parameter(value = 0)
  public QueryExpression<Object> queryExpression;

  @Parameter(value = 1)
  public Object expectation;


  @Test
  public void shouldBuildQueryFromSearchExpression() {
    // when
    final Query result = QueryUtils.getQuery(queryExpression);
    // then
    LOGGER.debug("Comparing {} vs {}", result, expectation);
    assertThat(result).isEqualTo(expectation);
  }

}
