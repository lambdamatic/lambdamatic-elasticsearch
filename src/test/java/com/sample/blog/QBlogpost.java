/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package com.sample.blog;

import org.lambdamatic.elasticsearch.annotations.DocumentField;
import org.lambdamatic.elasticsearch.querydsl.types.FullTextField;
import org.lambdamatic.internal.elasticsearch.QueryMetadata;

/**
 * Generated.
 */
// FIXME: this class MUST be generated
public class QBlogpost implements QueryMetadata<Blogpost> {

  @DocumentField(name = "title")
  public FullTextField title;
  
  @DocumentField(name = "comments")
  public QComments comments;
  
  public static class QComments implements QueryMetadata<Comment> {

    /** any comment. */
    @DocumentField(name = "comment")
    public CommentField comment;
    
    public interface CommentField extends FullTextField {
      
    }
  }

}
