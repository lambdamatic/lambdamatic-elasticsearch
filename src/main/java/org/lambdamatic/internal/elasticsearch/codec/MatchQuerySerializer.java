/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.internal.elasticsearch.codec;

import java.io.IOException;

import org.lambdamatic.internal.elasticsearch.searchdsl.MatchQuery;
import org.lambdamatic.internal.elasticsearch.searchdsl.TermQuery;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * A custom {@link JsonSerializer} for {@link TermQuery} objects.
 */
public class MatchQuerySerializer extends JsonSerializer<MatchQuery> {

  @Override
  public void serialize(final MatchQuery matchQuery, final JsonGenerator generator,
      final SerializerProvider serializers) throws IOException, JsonProcessingException {
    generator.writeStartObject();
    generator.writeObjectFieldStart("match");
    // TODO: add a flag to indicate that custom settings were set if we need to support more than just 'boost'
    if (matchQuery.getBoostFactor() != 1.0f) {
      generator.writeObjectFieldStart(matchQuery.getFieldName());
      generator.writeObjectField("query", matchQuery.getValue());
      generator.writeNumberField("boost", matchQuery.getBoostFactor());
      generator.writeEndObject();
    } else {
      generator.writeObjectField(matchQuery.getFieldName(), matchQuery.getValue());
    }
    generator.writeEndObject();
    generator.writeEndObject();
  }

}
