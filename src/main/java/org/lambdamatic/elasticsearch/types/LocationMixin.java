/*******************************************************************************
 * Copyright (c) 2014, 2015 Red Hat, Inc. Distributed under license by Red Hat, Inc. All rights
 * reserved. This program is made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/

package org.lambdamatic.elasticsearch.types;

import org.lambdamatic.internal.elasticsearch.codec.Mixin;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Jackson mixin for the {@link Location} type.
 * 
 * @author Xavier Coulon
 *
 */
@Mixin(target = Location.class)
public class LocationMixin {

  /** The latitude value. */
  @JsonProperty("lat")
  private double latitude;

  /** The longitude value. */
  @JsonProperty("lon")
  private double longitude;


}
