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

package com.sample;

import java.util.Date;

import org.lambdamatic.elasticsearch.annotations.DocumentField;

/**
 * A blog post comment
 */
public class Comment {

  @DocumentField
  private String name;

  @DocumentField
  private String comment;
  
  @DocumentField
  private int age;
  
  @DocumentField
  private int stars;
  
  @DocumentField
  private Date date;
}
