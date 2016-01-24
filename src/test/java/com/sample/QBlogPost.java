/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package com.sample;

import org.lambdamatic.elasticsearch.annotations.DocumentField;
import org.lambdamatic.elasticsearch.search.SearchableElement;
import org.lambdamatic.internal.elasticsearch.QueryMetadata;

/**
 * Generated.
 */
// FIXME: this class MUST be generated
public class QBlogPost implements QueryMetadata<BlogPost> {

  @DocumentField(name = "title")
  public SearchableElement title;
}
