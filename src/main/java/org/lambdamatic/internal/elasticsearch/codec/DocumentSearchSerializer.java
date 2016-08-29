/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.internal.elasticsearch.codec;

import java.io.IOException;

import org.lambdamatic.internal.elasticsearch.searchdsl.DocumentSearch;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * A custom {@link JsonSerializer} for {@link DocumentSearch} objects.
 */
public class DocumentSearchSerializer extends JsonSerializer<DocumentSearch> {

  @Override
  public void serialize(final DocumentSearch documentSearch, final JsonGenerator generator,
      final SerializerProvider serializers) throws IOException, JsonProcessingException {
    generator.writeStartObject();
    generator.writeObjectFieldStart("query");
    generator.writeObjectFieldStart("bool");
    if (documentSearch.getShouldMatchQuery() != null) {
      generator.writeObjectField("should", documentSearch.getShouldMatchQuery());
    }
    if (documentSearch.getMustMatchQuery() != null) {
      generator.writeObjectField("must", documentSearch.getMustMatchQuery());
    }
    if (documentSearch.getMustNotMatchQuery() != null) {
      generator.writeObjectField("must_not", documentSearch.getMustNotMatchQuery());
    }
    if (documentSearch.getFilterQuery() != null) {
      generator.writeObjectField("filter", documentSearch.getFilterQuery());
    }
    generator.writeEndObject(); // end "bool"
    generator.writeEndObject(); // end "query"
    // TODO: other search criteria (sort, from/size, etc.) come here
    generator.writeEndObject(); // end root

  }

}
