/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package com.sample.blog;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.sample.citybikesnyc.BikeStationStatus;

/**
 * An {@link Enum} for the status of a {@link Blogpost}.
 */
public enum BlogpostStatus {

  PUBLISHED, DRAFT;

  /**
   * Returns the {@link BlogpostStatus} corresponding to the given {@code literal}, or
   * {@code BlogpostStatus#DRAFT} if the given key is unknown.
   * 
   * @param literal the literal to process
   * @return the corresponding {@link BlogpostStatus}
   */
  @JsonCreator
  public static BlogpostStatus from(final String literal) {
    if (literal.equals(PUBLISHED.name().toLowerCase())) {
      return PUBLISHED;
    }
    return DRAFT;
  }

  @JsonValue
  public String getValue() {
    return this.name().toLowerCase();
  }
}
