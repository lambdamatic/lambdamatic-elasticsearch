/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package com.sample.acme;

import java.time.LocalDate;
import java.time.LocalTime;

import org.lambdamatic.elasticsearch.annotations.Document;
import org.lambdamatic.elasticsearch.annotations.DocumentField;

/**
 * A sample class to verify serialization/deserialization.
 */
@Document(index = "fooIndex", type = "fooType")
public class Foo {

  @DocumentField(name = "aName")
  private String name;

  @DocumentField(name = "aTime")
  private LocalTime time;
  
  @DocumentField(name = "aDate")
  private LocalDate date;
  
  @DocumentField(name = "aDate")
  private EnumFooType type;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public LocalTime getTime() {
    return time;
  }

  public void setTime(LocalTime time) {
    this.time = time;
  }

  public LocalDate getDate() {
    return date;
  }

  public void setDate(LocalDate date) {
    this.date = date;
  }

  public EnumFooType getType() {
    return type;
  }

  public void setType(EnumFooType type) {
    this.type = type;
  }
  
  

}
