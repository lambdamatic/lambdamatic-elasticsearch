/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.internal.elasticsearch.codec;

import java.io.IOException;

import org.lambdamatic.internal.elasticsearch.searchdsl.GeoBoundingBoxQuery;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * A {@link JsonSerializer} for the {@link GeoBoundingBoxQuery}.
 */
public class GeoBoundingBoxSerializer extends JsonSerializer<GeoBoundingBoxQuery> {

  @Override
  public void serialize(final GeoBoundingBoxQuery query, final JsonGenerator generator,
      final SerializerProvider serializers) throws IOException, JsonProcessingException {

    generator.writeStartObject();
    generator.writeObjectFieldStart("geo_bounding_box");
    generator.writeObjectFieldStart(query.getFieldName());
    generator.writeObjectFieldStart("top_left");
    generator.writeNumberField("lat", query.getTopLeftLatitude());
    generator.writeNumberField("lon", query.getTopLeftLongitude());
    generator.writeEndObject(); // end 'top_left'
    generator.writeObjectFieldStart("bottom_right");
    generator.writeNumberField("lat", query.getBottomRightLatitude());
    generator.writeNumberField("lon", query.getBottomRightLongitude());
    generator.writeEndObject(); // end 'bottom_right'
    generator.writeEndObject(); // end 'field name'
    generator.writeEndObject(); // end 'geo_bounding_box'
  }

}
