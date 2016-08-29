/**
 * 
 */
package com.sample.citybikesnyc;

import java.util.Date;

import org.lambdamatic.elasticsearch.annotations.Document;
import org.lambdamatic.elasticsearch.annotations.DocumentField;
import org.lambdamatic.elasticsearch.annotations.DocumentIdField;
import org.lambdamatic.elasticsearch.annotations.FullText;
import org.lambdamatic.elasticsearch.types.Location;

/**
 * A Bike Station document.
 * 
 */
@Document(index = "bikestation_index", type = "bikestation")
public class BikeStation {

  @DocumentIdField
  private String id;

  @DocumentField(name = "station_name")
  @FullText
  private String stationName;

  @DocumentField
  private int availableDocks;

  @DocumentField
  private int totalDocks;

  @DocumentField
  private int availableBikes;

  @DocumentField
  private Location location;

  @DocumentField
  private BikeStationStatus status;

  @DocumentField
  private boolean testStation;

  @DocumentField
  private Date executionTime;

  public String getId() {
    return id;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public String getStationName() {
    return stationName;
  }

  public void setStationName(final String stationName) {
    this.stationName = stationName;
  }

  public int getAvailableDocks() {
    return availableDocks;
  }

  public void setAvailableDocks(final int availableDocks) {
    this.availableDocks = availableDocks;
  }

  public int getTotalDocks() {
    return totalDocks;
  }

  public void setTotalDocks(final int totalDocks) {
    this.totalDocks = totalDocks;
  }

  public int getAvailableBikes() {
    return availableBikes;
  }

  public void setAvailableBikes(final int availableBikes) {
    this.availableBikes = availableBikes;
  }

  public Location getLocation() {
    return location;
  }

  public void setLocation(final Location location) {
    this.location = location;
  }

  public BikeStationStatus getStatus() {
    return status;
  }

  public void setStatus(final BikeStationStatus status) {
    this.status = status;
  }

  public boolean isTestStation() {
    return testStation;
  }

  public void setTestStation(final boolean testStation) {
    this.testStation = testStation;
  }

  public Date getExecutionTime() {
    return executionTime;
  }

  public void setExecutionTime(final Date executionTime) {
    this.executionTime = executionTime;
  }

  @Override
  public String toString() {
    return "Bike station " + stationName + " available docks:" + availableDocks
        + " / available bikes:" + availableBikes;
  }


}
