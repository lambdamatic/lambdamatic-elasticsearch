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
 * A Term {@link Query}.
 */
public class RangeQuery extends BaseQuery {

  public enum RangeType {
    GT, GTE, LT, LTE, BETWEEN;
  }
  
  private final RangeType type;
  
  private final Object value;
  
  public RangeQuery(final RangeType type, final String fieldName, final Object value) {
    super(fieldName);
    this.value = value;
    this.type = type;
  }
  
  public RangeType getType() {
    return this.type;
  }
  
  public Object getValue() {
    return this.value;
  }
  
  @Override
  public String toString() {
    return "TermQuery: (" + getFieldName() + "=" + value + ")^" + getBoostFactor();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((value == null) ? 0 : value.hashCode());
    return result;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    RangeQuery other = (RangeQuery) obj;
    if (value == null) {
      if (other.value != null) {
        return false;
      }
    } else if (!value.equals(other.value)) {
      return false;
    }
    return true;
  }

  
}
