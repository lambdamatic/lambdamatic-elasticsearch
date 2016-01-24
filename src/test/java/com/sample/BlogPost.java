/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package com.sample;

import java.util.List;

import org.lambdamatic.elasticsearch.annotations.Document;
import org.lambdamatic.elasticsearch.annotations.DocumentField;

/**
 * A blog post.
 */
@Document(indexName = "blog_index", type = "blogpost")
public class BlogPost {

  @DocumentField(id = true)
  private Long id;

  @DocumentField
  private String title;

  @DocumentField
  private String body;

  // @StringField
  // private String[] tags;

  // @StringField
  // private List<Comment> comments;
}
