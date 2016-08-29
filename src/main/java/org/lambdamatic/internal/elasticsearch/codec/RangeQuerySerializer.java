/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.internal.elasticsearch.codec;

import java.io.IOException;

import org.lambdamatic.elasticsearch.exceptions.CodecException;
import org.lambdamatic.internal.elasticsearch.searchdsl.RangeQuery;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * A custom {@link JsonSerializer} for {@link RangeQuery} objects.
 */
public class RangeQuerySerializer extends JsonSerializer<RangeQuery> {

  @Override
  public void serialize(final RangeQuery rangeQuery, final JsonGenerator generator,
      final SerializerProvider serializers) throws IOException, JsonProcessingException {
    generator.writeStartObject();
    generator.writeObjectFieldStart("range");
    generator.writeObjectFieldStart(rangeQuery.getFieldName());
    switch (rangeQuery.getType()) {
      case GT:
        generator.writeObjectField("gt", rangeQuery.getValue());
        break;
      case GTE:
        generator.writeObjectField("gte", rangeQuery.getValue());
        break;
      default:
        throw new CodecException("Unsupported (yet range type: " + rangeQuery.getType());
    }
    if (rangeQuery.getBoostFactor() != 1.0f) {
      generator.writeNumberField("boost", rangeQuery.getBoostFactor());
    }
    generator.writeEndObject();
    generator.writeEndObject();
    generator.writeEndObject();
  }
}
