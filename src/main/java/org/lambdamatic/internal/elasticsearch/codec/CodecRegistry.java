/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.internal.elasticsearch.codec;

import java.util.HashMap;
import java.util.Map;

import org.lambdamatic.elasticsearch.exceptions.CodecException;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * A registry for all {@link DocumentCodec}s.
 */
public class CodecRegistry {

  /**
   * The {@link DocumentCodec}s indexed by the fully qualified name of the domain types they can
   * encode/decode.
   */
  private final Map<String, DocumentCodec<?>> codecs = new HashMap<>();
  
  private final DocumentSearchCodec documentSearchCodec = new DocumentSearchCodec();

  /**
   * Registers a new {@link DocumentCodec} for a given type.
   * 
   * @param targetClass the type associated with the {@link DocumentCodec}
   */
  public <T> void registerCodec(final Class<T> targetClass) {
    this.codecs.put(targetClass.getName(),
        new DocumentCodec<>(targetClass, ObjectMapperFactory.getObjectMapper()));
  }

  /**
   * Retrieves the {@link DocumentCodec} associated with the given {@code domainObject}.
   * 
   * @param domainObject the document to encode or decode
   * @return the {@link DocumentCodec} for the given {@code domainObject}
   * @throws CodecException if no {@link DocumentCodec} was found
   */
  @SuppressWarnings("unchecked")
  public <T> DocumentCodec<T> getDocumentCodec(final T domainObject) {
    final String domainType = domainObject.getClass().getName();
    if (!this.codecs.containsKey(domainType)) {
      throw new CodecException("Unable to locate a document codec for type " + domainType);
    }
    return (DocumentCodec<T>) this.codecs.get(domainType);
  }

  /**
   * Retrieves the {@link DocumentCodec} associated with the given {@code domainType}.
   * 
   * @param domainType the type of the document to encode or decode
   * @return the {@link DocumentCodec} for the given {@code domainType}
   * @throws CodecException if no {@link DocumentCodec} was found
   */
  public <T> DocumentCodec<T> getDocumentCodec(final Class<T> domainType) {
    return getDocumentCodec(domainType.getName());
  }

  /**
   * Retrieves the {@link DocumentCodec} associated with the given {@code domainType}.
   * 
   * @param domainType the type of the document to encode or decode
   * @return the {@link DocumentCodec} for the given {@code domainType}
   * @throws CodecException if no {@link DocumentCodec} was found
   */
  @SuppressWarnings("unchecked")
  public <T> DocumentCodec<T> getDocumentCodec(final String domainType) {
    if (!this.codecs.containsKey(domainType)) {
      throw new CodecException("Unable to locate a document codec for type " + domainType);
    }
    return (DocumentCodec<T>) this.codecs.get(domainType);
  }

  /**
   * Retrieves the {@link DocumentCodec} associated with the given {@code domainType}.
   * 
   * @param defaultDomainType the default type of the document to encode or decode
   * @return the {@link DocumentCodec} for the given {@code domainType}
   * @throws CodecException if no {@link DocumentCodec} was found
   */
  @SuppressWarnings("unchecked")
  public <T> DocumentCodec<T> getDocumentCodec(final JsonNode source, final Class<T> defaultDomainType) {
    if (source.get(DocumentCodec.DOMAIN_TYPE) != null) {
      final String domainType = source.get(DocumentCodec.DOMAIN_TYPE).toString();
      return getDocumentCodec(domainType);
    }
    // fall-back to the codec for the default domain type if the 'DOMAIN_TYPE' field is missing
    return (DocumentCodec<T>) this.codecs.get(defaultDomainType.getName());
  }

  public DocumentSearchCodec getDocumentQueryCodec() {
    return this.documentSearchCodec;
  }

}
