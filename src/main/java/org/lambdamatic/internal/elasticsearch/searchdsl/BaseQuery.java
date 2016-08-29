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

package org.lambdamatic.internal.elasticsearch.searchdsl;

/**
 * Base class for all {@link Query} implementations.
 */
public class BaseQuery implements Query {

  private final String fieldName;
  
  private float boostFactor = 1.0f;
  
  public BaseQuery(final String fieldName) {
    this.fieldName = fieldName;
  }
  
  public String getFieldName() {
    return this.fieldName;
  }
  
  public float getBoostFactor() {
    return this.boostFactor;
  }
  
  @Override
  public Query boost(final float boostFactor) {
    this.boostFactor = boostFactor;
    return this;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Float.floatToIntBits(boostFactor);
    result = prime * result + ((fieldName == null) ? 0 : fieldName.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    BaseQuery other = (BaseQuery) obj;
    if (Float.floatToIntBits(boostFactor) != Float.floatToIntBits(other.boostFactor)) {
      return false;
    }
    if (fieldName == null) {
      if (other.fieldName != null) {
        return false;
      }
    } else if (!fieldName.equals(other.fieldName)) {
      return false;
    }
    return true;
  }
  
  

}
