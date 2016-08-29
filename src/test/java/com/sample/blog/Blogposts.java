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

import org.lambdamatic.internal.elasticsearch.BaseDocumentManagerImpl;
import org.lambdamatic.internal.elasticsearch.clientdsl.Client;

/**
 * Manager for the {@link Blogpost} documents stored in Elasticsearch.
 */
public class Blogposts extends BaseDocumentManagerImpl<Blogpost, QBlogpost> {

  public static final String BLOGPOST_INDEX_NAME = "blogposts";

  public static final String BLOGPOST_TYPE = "blogpost";
  
  public Blogposts(final Client client) {
    super(client, Blogpost.class);
  }

}
