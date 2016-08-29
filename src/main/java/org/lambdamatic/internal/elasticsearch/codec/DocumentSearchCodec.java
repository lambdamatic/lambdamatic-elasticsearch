/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.internal.elasticsearch.codec;

import org.lambdamatic.elasticsearch.exceptions.CodecException;
import org.lambdamatic.internal.elasticsearch.searchdsl.BooleanQuery;
import org.lambdamatic.internal.elasticsearch.searchdsl.DocumentSearch;
import org.lambdamatic.internal.elasticsearch.searchdsl.GeoBoundingBoxQuery;
import org.lambdamatic.internal.elasticsearch.searchdsl.MatchQuery;
import org.lambdamatic.internal.elasticsearch.searchdsl.RangeQuery;
import org.lambdamatic.internal.elasticsearch.searchdsl.TermQuery;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * A utility class to encode {@link DocumentSearch} objects.
 * 
 */
public class DocumentSearchCodec {

  /**
   * The {@link ObjectMapper} to use to encode a {@link DocumentSearch}.
   */
  private final ObjectMapper objectMapper;

  /**
   * Constructor.
   * 
   */
  public DocumentSearchCodec() {
    this.objectMapper = new ObjectMapper();
    // custom serializers for queries
    final SimpleModule module = new SimpleModule();
    module.addSerializer(DocumentSearch.class, new DocumentSearchSerializer());
    module.addSerializer(TermQuery.class, new TermQuerySerializer());
    module.addSerializer(RangeQuery.class, new RangeQuerySerializer());
    module.addSerializer(MatchQuery.class, new MatchQuerySerializer());
    module.addSerializer(BooleanQuery.class, new BooleanQuerySerializer());
    module.addSerializer(GeoBoundingBoxQuery.class, new GeoBoundingBoxSerializer());
    this.objectMapper.registerModule(module);
    // support for Java time
    this.objectMapper.registerModule(new JavaTimeModule());
    // configure LocalDate serialization as string with pattern: YYYY-mm-dd
    this.objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

  }

  public <D, Q, F> String encode(final DocumentSearch documentSearch) {
    try {
      return this.objectMapper.writeValueAsString(documentSearch);
    } catch (JsonProcessingException e) {
      throw new CodecException("Failed to convert search request into a JSON document", e);
    }
  }

}
