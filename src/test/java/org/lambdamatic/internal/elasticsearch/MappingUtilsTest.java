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

package org.lambdamatic.internal.elasticsearch;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.Test;

import com.sample.blog.BlogPost;

/**
 * Testing the {@link MappingUtils} utility class.
 */
public class MappingUtilsTest {

  @Test
  public void shouldBuildMapping() {
    // given
    // when
    final Map<String, Object> classMapping = MappingUtils.getClassMapping(BlogPost.class);
    // then
    assertThat(classMapping.get("properties")).isInstanceOf(Map.class);
    final Map<String, Object> blogPostMappingProperties = (Map<String, Object>) classMapping.get("properties");
    assertThat(blogPostMappingProperties.get("comments")).isInstanceOf(Map.class);
    final Map<String, Object> commentsMapping = (Map<String, Object>) blogPostMappingProperties.get("comments");
    assertThat(commentsMapping.get("type")).isEqualTo("object");
    assertThat(commentsMapping.get("properties")).isInstanceOf(Map.class);
    final Map<String, Object> commentsMappingProperties = (Map<String, Object>) commentsMapping.get("properties");
  }
}
