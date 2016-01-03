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

import static org.assertj.core.api.Assertions.assertThat;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.junit.Rule;
import org.junit.Test;
import org.lambdamatic.elasticsearch.annotations.Document;
import org.lambdamatic.elasticsearch.utils.ClientRule;
import org.lambdamatic.internal.elasticsearch.IndexMappingValidator;
import org.lambdamatic.internal.elasticsearch.IndexStatus;
import org.lambdamatic.internal.elasticsearch.MappingUtils;

import com.sample.BlogPost;
import com.sample.BlogPostIndex;

/**
 * 
 */
public class IndexMappingValidatorTest {

  @Rule
  public final ClientRule clientRule = new ClientRule();
  
  @Test
  public void shouldCreateIndex() {
    // given
    final Client client = clientRule.getClient();
    client.admin().indices().delete(
        Requests.deleteIndexRequest(BlogPost.class.getAnnotation(Document.class).indexName()));
    final BlogPostIndex blogPostIndex = new BlogPostIndex(client);
    // when
    final IndexStatus status = blogPostIndex.verifyIndex();
    // then
    assertThat(status).isEqualTo(IndexStatus.OK);
  }
  @Test
  public void shouldLocateIdFieldInDomainType() {
    // when
    final String idFieldName = MappingUtils.getIdFieldName(BlogPost.class);
    // then
    assertThat(idFieldName).isEqualTo("id");
  }
}
