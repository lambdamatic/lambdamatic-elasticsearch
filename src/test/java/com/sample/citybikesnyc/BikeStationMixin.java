/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package com.sample.citybikesnyc;

import java.util.Date;

import org.lambdamatic.elasticsearch.types.Location;
import org.lambdamatic.internal.elasticsearch.codec.Mixin;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Jackson mixin for the {@link BikeStation} domain type.
 */
@Mixin(target = BikeStation.class)
public class BikeStationMixin {

  @JsonProperty("stationName")
  private String stationName;

  @JsonProperty("availableDocks")
  private int availableDocks;

  @JsonProperty("totalDocks")
  private int totalDocks;

  @JsonProperty("availableBikes")
  private int availableBikes;

  @JsonProperty("location")
  private Location location;

  @JsonProperty("status")
  private BikeStationStatus status;

  @JsonProperty("testStation")
  private boolean testStation;

  @JsonProperty("executionTime")
  private Date executionTime;
}
