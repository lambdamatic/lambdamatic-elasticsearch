/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package com.sample.blog;

import java.util.List;

import org.lambdamatic.internal.elasticsearch.codec.Mixin;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Jackson mixin for the {@link Blogpost} domain type.
 */
@Mixin(target = Blogpost.class)
public class BlogpostMixin {

  @JsonProperty
  private String title;

  @JsonProperty
  private String body;

  @JsonProperty
  private String[] tags;

  @JsonProperty
  private List<Comment> comments;

}
