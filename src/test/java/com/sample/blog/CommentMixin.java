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
import org.lambdamatic.elasticsearch.annotations.EmbeddedDocument;
import org.lambdamatic.internal.elasticsearch.codec.Mixin;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Jackson mixin for the {@link Comment} domain type.
 */
@Mixin(target = Comment.class)
public class CommentMixin {

  @JsonProperty
  private String authorName;

  @JsonProperty
  private String comment;

  @JsonProperty
  private int stars;

  @JsonProperty
  private LocalDate date;

}
