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

package com.sample.blog;

import org.elasticsearch.client.Client;
import org.lambdamatic.internal.elasticsearch.BaseElasticsearchDomainTypeManagerImpl;

/**
 * Manager for the {@link BlogPost} entities stored in Elasticsearch.
 */
public class BlogPosts extends BaseElasticsearchDomainTypeManagerImpl<BlogPost, QBlogPost> {

  public BlogPosts(final Client client) {
    super(client, BlogPost.class);
  }
  
}
