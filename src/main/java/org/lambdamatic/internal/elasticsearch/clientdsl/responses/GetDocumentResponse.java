/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.internal.elasticsearch.clientdsl.responses;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Response to the request to get a document.
 */
public class GetDocumentResponse {

  @JsonProperty("found")
  private boolean exists;

  @JsonProperty("_source")
  @JsonDeserialize(using = DocumentSourceDeserializer.class)
  private JsonNode source;

  public boolean isExists() {
    return this.exists;
  }

  public JsonNode getSource() {
    return this.source;
  }

  static class DocumentSourceDeserializer extends JsonDeserializer<JsonNode> {

    @Override
    public JsonNode deserialize(JsonParser jsonParser, DeserializationContext context)
        throws IOException, JsonProcessingException {
      return jsonParser.readValueAsTree();
    }

  }
}
