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
import org.lambdamatic.internal.elasticsearch.searchdsl.BooleanQuery;
import org.lambdamatic.internal.elasticsearch.searchdsl.Query;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * A custom {@link JsonSerializer} for {@link BooleanQuery} objects.
 */
public class BooleanQuerySerializer extends JsonSerializer<BooleanQuery> {

  @Override
  public void serialize(final BooleanQuery booleanQuery, final JsonGenerator generator,
      final SerializerProvider serializers) throws IOException, JsonProcessingException {
    // if the booleanQuery does not contain other boolean queries in its elements, just write the
    // elements as an array
    if (!containsBooleanQueries(booleanQuery)) {
      generator.writeStartArray();
      for (Query query : booleanQuery.getQueries()) {
        generator.writeObject(query);
      }
      generator.writeEndArray();
    } else {
      // otherwise...
      generator.writeStartObject();
      generator.writeObjectFieldStart("bool");
      switch (booleanQuery.getType()) {
        case AND:
          // TODO: also support 'must_not' if the query is inverted.
          generator.writeArrayFieldStart("must");
          for (Query query : booleanQuery.getQueries()) {
            generator.writeObject(query);
          }
          generator.writeEndArray(); // end of 'must'
          break;
        case OR:
          generator.writeArrayFieldStart("should");
          for (Query query : booleanQuery.getQueries()) {
            generator.writeObject(query);
          }
          generator.writeEndArray();// end of 'should'
          break;
        default:
          throw new CodecException("Unexpected boolean type:" + booleanQuery.getType());
      }
      generator.writeEndObject(); // end of 'bool'
      generator.writeEndObject(); // end of root
    }
  }

  private static boolean containsBooleanQueries(final BooleanQuery booleanQuery) {
    return booleanQuery.getQueries().stream().anyMatch(q -> q instanceof BooleanQuery);
  }

}
