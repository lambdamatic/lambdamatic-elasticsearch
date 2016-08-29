/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package com.sample.blog;

import java.time.LocalDate;

import org.lambdamatic.elasticsearch.annotations.DocumentField;
import org.lambdamatic.elasticsearch.annotations.FullText;
import org.lambdamatic.elasticsearch.annotations.Keyword;
import org.lambdamatic.elasticsearch.searchdsl.types.FullTextField;
import org.lambdamatic.elasticsearch.searchdsl.types.KeywordField;
import org.lambdamatic.internal.elasticsearch.QueryMetadata;

/**
 * Generated.
 */
// FIXME: this class MUST be generated
public class QBlogpost implements QueryMetadata<Blogpost> {

  @DocumentField(name = "title")
  @FullText
  public FullTextField title;
  
  @DocumentField(name = "content")
  @FullText
  public FullTextField content;
  
  @DocumentField(name = "status")
  @Keyword
  public KeywordField<BlogpostStatus> status;
  
  @DocumentField(name = "publish_date")
  @Keyword
  public KeywordField<LocalDate> publishDate;
  
  @DocumentField(name = "comments")
  public QComments comments;
  
  public static class QComments implements QueryMetadata<Comment> {

    /** any comment. */
    @DocumentField(name = "comment")
    @FullText
    public FullTextField comment;
    
  }

}
