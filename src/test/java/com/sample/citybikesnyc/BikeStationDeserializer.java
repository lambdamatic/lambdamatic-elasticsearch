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

package com.sample.citybikesnyc;

import java.io.IOException;

import org.lambdamatic.elasticsearch.types.Location;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * a {@link JsonDeserializer} for the {@link BikeStation} type.
 */
public class BikeStationDeserializer extends StdDeserializer {

  public BikeStationDeserializer() {
    super(BikeStation.class);
  }
  
  @Override
  public Object deserialize(final JsonParser jp, final DeserializationContext ctxt)
      throws IOException, JsonProcessingException {
    final BikeStation bikeStation = new BikeStation();
    final JsonNode rootNode = jp.getCodec().readTree(jp);
    final String stationName = rootNode.get("stationName").textValue();
    bikeStation.setStationName(stationName);
    final int availableDocks = (Integer) (rootNode.get("availableDocks")).numberValue();
    bikeStation.setAvailableDocks(availableDocks);
    final int status = (Integer) (rootNode.get("status")).numberValue();
    bikeStation.setStatus(BikeStationStatus.valueOf(status));
    
    final ObjectNode locationNode = (ObjectNode) rootNode.get("location");
    final JavaType locationType = ctxt.constructType(Location.class);
    final JsonParser locationNodeParser = jp.getCodec().getFactory().createParser(locationNode.toString());
    
    final Location locationValue = (Location) ctxt.findNonContextualValueDeserializer(locationType)
        .deserialize(locationNodeParser, ctxt);
    bikeStation.setLocation(locationValue);
    return bikeStation;
  }

}
