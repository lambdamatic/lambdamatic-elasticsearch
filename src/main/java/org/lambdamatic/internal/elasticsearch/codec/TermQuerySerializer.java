/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.internal.elasticsearch.codec;

import java.io.IOException;

import org.lambdamatic.internal.elasticsearch.searchdsl.TermQuery;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * A custom {@link JsonSerializer} for {@link TermQuery} objects.
 */
public class TermQuerySerializer extends JsonSerializer<TermQuery> {

  @Override
  public void serialize(final TermQuery termQuery, final JsonGenerator generator,
      final SerializerProvider serializers) throws IOException, JsonProcessingException {
    generator.writeStartObject();
    generator.writeObjectFieldStart("term");
    // TODO: add a flag to indicate that custom settings were set if we need to support more than just 'boost'
    if (termQuery.getBoostFactor() != 1.0f) {
      generator.writeObjectFieldStart(termQuery.getFieldName());
      generator.writeObjectField("value", termQuery.getValue());
      generator.writeNumberField("boost", termQuery.getBoostFactor());
      generator.writeEndObject();
    } else {
      generator.writeObjectField(termQuery.getFieldName(), termQuery.getValue());
    }
    generator.writeEndObject();
    generator.writeEndObject();
  }

}
