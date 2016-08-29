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
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * {@link JsonSerializer} for the {@link DocumentSearch} type.
 */
public class SearchRequestSerializer extends StdSerializer<DocumentSearch> {

  /** generated serial version. */
  private static final long serialVersionUID = 1836677269766626523L;

  /**
   * Constructor.
   */
  public SearchRequestSerializer() {
    super(DocumentSearch.class, true);
  }
  
  @Override
  public void serialize(final DocumentSearch searchRequest, final JsonGenerator generator,
      final SerializerProvider provider) throws IOException {
    
  }

}
