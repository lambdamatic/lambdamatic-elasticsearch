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

package org.lambdamatic.elasticsearch;

import org.assertj.core.api.Assertions;
import org.elasticsearch.client.Requests;
import org.elasticsearch.test.ESSingleNodeTestCase;
import org.junit.Test;
import org.lambdamatic.elasticsearch.annotations.Document;
import org.lambdamatic.internal.elasticsearch.IndexMappingValidator;
import org.lambdamatic.internal.elasticsearch.IndexValidationStatus;
import org.lambdamatic.internal.elasticsearch.MappingUtils;

import com.sample.BlogPost;
import com.sample.BlogPosts;

/**
 * Testing the {@link IndexMappingValidator}.
 */
public class IndexMappingValidatorTest extends ESSingleNodeTestCase {

  @Test
  public void shouldCreateIndex() {
    // given
    client().admin().indices().delete(
        Requests.deleteIndexRequest(BlogPost.class.getAnnotation(Document.class).indexName()));
    final BlogPosts blogPostIndex = new BlogPosts(client());
    // when
    final IndexValidationStatus status = blogPostIndex.verifyIndex();
    // then
    Assertions.assertThat(status).isEqualTo(IndexValidationStatus.OK);
  }
  
  @Test
  public void shouldLocateIdFieldInDomainType() {
    // when
    final String idFieldName = MappingUtils.getIdFieldName(BlogPost.class);
    // then
    Assertions.assertThat(idFieldName).isEqualTo("id");
  }
}
