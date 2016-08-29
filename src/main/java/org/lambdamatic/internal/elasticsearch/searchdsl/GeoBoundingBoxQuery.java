/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.internal.elasticsearch.searchdsl;

/**
 * A geo bounding box {@link Query}.
 */
public class GeoBoundingBoxQuery extends BaseQuery {

  public static Builder fieldName(final String fieldName) {
    return new Builder(fieldName);
  }
  
  public static class Builder {

    private final String fieldName;
    
    private double topLeftLatitude;

    private double topLeftLongitude;

    private double bottomRightLatitude;

    private double bottomRightLongitude;

    /**
     * Constructor.
     * @param fieldName
     */
    private Builder(final String fieldName) {
      this.fieldName = fieldName;
    }

    public Builder topLeft(final double topLeftLatitude, final double topLeftLongitude) {
      this.topLeftLatitude = topLeftLatitude;
      this.topLeftLongitude = topLeftLongitude;
      return this;
    }

    public GeoBoundingBoxQuery bottomRight(final double bottomRightLatitude,
        final double bottomRightLongitude) {
      this.bottomRightLatitude = bottomRightLatitude;
      this.bottomRightLongitude = bottomRightLongitude;
      return new GeoBoundingBoxQuery(this);
    }

  }

  private final double topLeftLatitude;
  
  private final double topLeftLongitude;
  
  private final double bottomRightLatitude;
  
  private final double bottomRightLongitude;


  private GeoBoundingBoxQuery(final Builder builder) {
    super(builder.fieldName);
    this.topLeftLatitude = builder.topLeftLatitude;
    this.topLeftLongitude = builder.topLeftLongitude;
    this.bottomRightLatitude = builder.bottomRightLatitude;
    this.bottomRightLongitude = builder.bottomRightLongitude;
  }

  @Override
  public String toString() {
    return "GeoBoundingBoxQuery: (" + getFieldName() + " within [" + topLeftLatitude + ", "
        + topLeftLongitude + ", " + bottomRightLatitude + ", " + bottomRightLongitude + "])^"
        + getBoostFactor();
  }

  public double getTopLeftLatitude() {
    return this.topLeftLatitude;
  }
  
  public double getTopLeftLongitude() {
    return this.topLeftLongitude;
  }
  
  public double getBottomRightLatitude() {
    return this.bottomRightLatitude;
  }
  
  public double getBottomRightLongitude() {
    return this.bottomRightLongitude;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(bottomRightLatitude);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(bottomRightLongitude);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(topLeftLatitude);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(topLeftLongitude);
    result = prime * result + (int) (temp ^ (temp >>> 32));
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
    GeoBoundingBoxQuery other = (GeoBoundingBoxQuery) obj;
    if (Double.doubleToLongBits(bottomRightLatitude) != Double
        .doubleToLongBits(other.bottomRightLatitude)) {
      return false;
    }
    if (Double.doubleToLongBits(bottomRightLongitude) != Double
        .doubleToLongBits(other.bottomRightLongitude)) {
      return false;
    }
    if (Double.doubleToLongBits(topLeftLatitude) != Double
        .doubleToLongBits(other.topLeftLatitude)) {
      return false;
    }
    if (Double.doubleToLongBits(topLeftLongitude) != Double
        .doubleToLongBits(other.topLeftLongitude)) {
      return false;
    }
    return true;
  }

}
