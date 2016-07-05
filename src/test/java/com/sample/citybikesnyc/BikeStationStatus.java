/**
 * 
 */
package com.sample.citybikesnyc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * The {@link BikeStation} status.
 * 
 * @author Xavier Coulon
 *
 */
public enum BikeStationStatus {

  IN_SERVICE(1), PLANNED(2), NOT_IN_SERVICE(3), UNKNOWN(4);

  public final int key;

  private BikeStationStatus(final int key) {
    this.key = key;
  }

  /**
   * Returns the {@link BikeStationStatus} corresponding to the given {@code key}, or
   * {@code BikeStationStatus#UNKNOWN} if the given key is unknown.
   * 
   * @param key the key to process
   * @return the corresponding {@link BikeStationStatus}
   */
  @JsonCreator
  public static BikeStationStatus valueOf(final int key) {
    switch (key) {
      case 1:
        return IN_SERVICE;
      case 2:
        return PLANNED;
      case 3:
        return NOT_IN_SERVICE;
      default:
        return UNKNOWN;
    }
  }
  
  @JsonValue
  public int getKey() {
    return key;
  }

}
