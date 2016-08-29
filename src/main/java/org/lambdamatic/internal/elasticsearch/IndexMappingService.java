/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.internal.elasticsearch;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.lambdamatic.elasticsearch.annotations.Document;
import org.lambdamatic.elasticsearch.annotations.DocumentField;
import org.lambdamatic.elasticsearch.annotations.FullText;
import org.lambdamatic.elasticsearch.exceptions.ClientResponseException;
import org.lambdamatic.internal.elasticsearch.clientdsl.Client;
import org.lambdamatic.internal.elasticsearch.clientdsl.responses.GetIndexMappingsResponse;
import org.lambdamatic.internal.elasticsearch.clientdsl.responses.GetIndexMappingsResponse.IndexMapping;
import org.lambdamatic.internal.elasticsearch.codec.DocumentCodec;

/**
 * Utility class to validate the mapping of a given Elasticsearch index with its associated domain
 * type.
 * 
 */
public class IndexMappingService {

  private final Client client;

  private final Class<?> domainType;

  private final String indexName;

  private final String typeName;

  private IndexValidationStatus indexStatus = null;

  /**
   * Constructor.
   * 
   * @param client the underlying Elasticsearch {@link Client}.
   * @param domainType the type of domain entity to map.
   * @param indexName the name of the index in Elasticsearch.
   * @param typeName the name of the type in the index.
   * 
   */
  public IndexMappingService(final Client client, final Class<?> domainType, final String indexName,
      final String typeName) {
    this.client = client;
    this.domainType = domainType;
    this.indexName = indexName;
    this.typeName = typeName;
  }

  /**
   * Verifies the status of the index in Elasticsearch:
   * <ul>
   * <li>If the index <strong>does not exist</strong>, it will be created.</li>
   * <li>If the index exists and <strong>matches</strong> the domain type mapping, it can be used
   * as-is.</li>
   * <li>If the index exists and <strong>does not match</strong> the domain type mapping, it cannot
   * be used and an exception will be raised.</li>
   * </ul>
   * 
   * @return The corresponding {@link IndexValidationStatus}
   */
  public IndexValidationStatus verifyIndex() {
    if (this.indexStatus == null) {
      try {
        final GetIndexMappingsResponse indexMappingsResponse =
            client.getIndexMappings(this.indexName, this.typeName);
        this.indexStatus = verifyMappings(indexMappingsResponse.getIndexMappings(this.indexName));
      } catch (ClientResponseException e) {
        if (e.getErrorResponse().getStatus() == 404) {
          // index does not exist yet, we need to create it
          this.indexStatus = createIndex();
        } else {
          // something really wrong happened
          throw e;
        }
      }
    }
    return this.indexStatus;
  }

  /**
   * Creates the index with the required mappings.
   * 
   * @return {@link IndexValidationStatus#OK} if the operation succeeded.
   */
  private IndexValidationStatus createIndex() {
    final Map<String, Object> indexSettings = null;
    final Map<String, Object> typeMapping = getDomainTypeMapping(this.domainType);
    client.createIndex(this.indexName, indexSettings);
    client.createIndexType(this.indexName, this.typeName, typeMapping);
    // client.createIndex(indexName, settings, mappings);
    // CreateIndexAction.INSTANCE.newRequestBuilder(this.client).setIndex(this.indexName)
    // .addMapping(this.typeName, mapping).get();
    return IndexValidationStatus.OK;
  }

  /**
   * Verifies that the mappings match the associated domain type.
   * 
   * @param mappings the mappings retrieved from the Elasticsearch index.
   * @return the {@link IndexValidationStatus}
   */
  @SuppressWarnings("static-method")
  private IndexValidationStatus verifyMappings(final IndexMapping mappings) {
    // TODO: implement code
    return IndexValidationStatus.OK;
  }

  /**
   * Builds the Elasticsearch mappings for the given {@code domainType}.
   * 
   * @param domainType the domain type to analyze
   * @return the mapping of each field in the given type
   */
  public static Map<String, Object> getDomainTypeMapping(final Class<?> domainType) {
    final Map<String, Object> fieldMappings = Stream.of(domainType.getDeclaredFields())
        .filter(f -> f.getAnnotation(DocumentField.class) != null).collect(
            Collectors.<Field, String, Object>toMap(f -> getFieldName(f), f -> getFieldMapping(f)));
    // also include a field to store the domain type
    fieldMappings.put(DocumentCodec.DOMAIN_TYPE, wrapFieldType("keyword"));
    // mapping properties for all document fields, including type and analyzers
    final Map<String, Object> classMapping = new HashMap<>();
    classMapping.put("properties", fieldMappings);
    return classMapping;
  }

  /**
   * Retrieves the name of the given domain type {@code field} that will be used in the
   * Elasticsearch document.
   * 
   * @param field the field to process
   * @return the value of the {@link DocumentField#name()} if the annotation was present and the
   *         {@code name} attribute was set, otherwise the name of the given {@code field}.
   */
  private static String getFieldName(final Field field) {
    final DocumentField documentFieldAnnotation = field.getAnnotation(DocumentField.class);
    if (documentFieldAnnotation != null && documentFieldAnnotation.name() != null
        && !documentFieldAnnotation.name().isEmpty()) {
      return documentFieldAnnotation.name();
    }
    return field.getName();
  }

  /**
   * Returns the Elasticsearch mapping for the given domain field.
   * 
   * @param field the field in the domain type
   * @return the corresponding type in the document stored in Elasticsearch
   * @see <a href="https://www.elastic.co/guide/en/elasticsearch/guide/current/mapping-intro.html">
   *      Elasticsearch Mapping documentation</a>
   */
  private static Map<String, Object> getFieldMapping(final Field field) {
    final String luceneFieldType = getLuceneFieldType(field, field.getType());
    final Map<String, Object> mapping = wrapFieldType(luceneFieldType);
    if (luceneFieldType.equals("object")) {
      final Type fieldGenericType = field.getGenericType();
      final Class<?> parameterType =
          (Class<?>) ((ParameterizedType) fieldGenericType).getActualTypeArguments()[0];
      final Map<String, Object> fieldProperties = getDomainTypeMapping(parameterType);
      mapping.put("properties", fieldProperties.get("properties"));
    }
    return mapping;
  }

  /**
   * Wraps the given {@code fieldType} as the value of an entry in a {@link Map}
   * 
   * @param fieldType
   * @return the resulting {@link Map}
   */
  private static Map<String, Object> wrapFieldType(final String fieldType) {
    final Map<String, Object> mapping = new HashMap<>();
    mapping.put("type", fieldType);
    return mapping;
  }

  /**
   * Returns the Elasticsearch field type for the given domain field. <br/>
   * Elasticsearch supports the following simple field types:
   * <ul>
   * <li>String: string</li>
   * <li>Whole number: byte, short, integer, long</li>
   * <li>Floating-point: float, double</li>
   * <li>Boolean: boolean</li>
   * <li>Date: date</li>
   * </ul>
   * 
   * @param fieldType the type of the field in the domain type
   * @return the corresponding type in the document stored in Elasticsearch
   * @see <a href="https://www.elastic.co/guide/en/elasticsearch/guide/current/mapping-intro.html">
   *      Elasticsearch Mapping documentation</a>
   */
  static String getLuceneFieldType(final Field field, final Class<?> fieldType) {
    if (fieldType == Boolean.class) {
      return "boolean";
    } else if (fieldType == Byte.class || fieldType == byte.class) {
      return "byte";
    } else if (fieldType == Short.class || fieldType == short.class) {
      return "short";
    } else if (fieldType == Integer.class || fieldType == int.class) {
      return "integer";
    } else if (fieldType == Long.class || fieldType == long.class) {
      return "long";
    } else if (fieldType == Float.class || fieldType == float.class) {
      return "float";
    } else if (fieldType == Double.class || fieldType == double.class) {
      return "double";
    } else if (fieldType == LocalDate.class) {
      return "date";
    } else if (fieldType == String.class || fieldType.isEnum()) {
      if (field.getAnnotation(FullText.class) != null) {
        return "text";
      }
      // assume the field is a 'keyword'
      return "keyword";
    } else if (fieldType.isArray()) {
      // FIXME: need to add and support @Nested:
      // https://www.elastic.co/guide/en/elasticsearch/reference/master/nested.html
      return getLuceneFieldType(field, fieldType.getComponentType());
    } else if (Collection.class.isAssignableFrom(fieldType)) {
      return "object";
    }
    throw new MappingException("Unable to retrieve the Lucene field type for " + fieldType);
  }


}
