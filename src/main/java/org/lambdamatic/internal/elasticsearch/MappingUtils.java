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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.lambdamatic.elasticsearch.annotations.DocumentField;
import org.lambdamatic.elasticsearch.annotations.DocumentId;
import org.lambdamatic.elasticsearch.utils.Pair;

/**
 * Utility class for domain class mapping in an Elasticsearch index.
 */
public interface MappingUtils {

  /**
   * Builds the mappings for a given domain type.
   * 
   * @param domainType the domain type to analyze
   * @return the mapping of each field in the given type
   */
  public static Map<String, Object> getClassMapping(final Class<?> domainType) {
    final Map<String, Object> classMapping = new HashMap<>();
    final Map<String, Object> fieldMappings = Stream.of(domainType.getDeclaredFields())
        .filter(f -> f.getAnnotation(DocumentField.class) != null).collect(
            Collectors.<Field, String, Object>toMap(f -> f.getName(), f -> getFieldMapping(f)));
    // mapping properties for all document fields, including type and analyzers
    classMapping.put("properties", fieldMappings);

    return classMapping;
  }


  /**
   * Analyze the given domain type and returns the name of its field to use as the document id, if
   * available.
   * 
   * @param domainType the domain type to analyze
   * @return the name of the single Java field annotated with {@link DocumentId}. If no field
   *         matches the criteria, <code>null</code> is returned. If more than one field is matches
   *         these criteria, a {@link MappingException} is thrown.
   */
  public static String getIdFieldName(Class<?> domainType) {
    final List<Pair<Field, DocumentId>> candidateFields =
        Stream.of(domainType.getDeclaredFields())
            .map(field -> new Pair<>(field, field.getAnnotation(DocumentId.class)))
            .filter(pair -> pair.getRight() != null).collect(Collectors.toList());
    if (candidateFields.isEmpty()) {
      return null;
    } else if (candidateFields.size() > 1) {
      final String fieldNames = candidateFields.stream().map(pair -> pair.getLeft().getName())
          .collect(Collectors.joining(", "));
      throw new MappingException(
          "More than one field is annotated with '@'" + DocumentId.class.getName() + ": {}",
          fieldNames);
    }
    final Field domainField = candidateFields.get(0).getLeft();
    return domainField.getName();
  }


  /**
   * Returns the Lucene field mapping for the given domain field.
   * 
   * @param field the field in the domain type
   * @return the corresponding type in the Lucene document stored in Elasticsearch
   * @see <a href="https://www.elastic.co/guide/en/elasticsearch/guide/current/mapping-intro.html">
   *      Elasticsearch Mapping documentation</a>
   */
  public static Object getFieldMapping(final Field field) {
    final Map<String, Object> mapping = new HashMap<>();
    final String luceneFieldType = getLuceneFieldType(field.getType());
    mapping.put("type", luceneFieldType);
    if (luceneFieldType.equals("object")) {
      final Type fieldGenericType = field.getGenericType();
      final Class<?> parameterType =
          (Class<?>) ((ParameterizedType) fieldGenericType).getActualTypeArguments()[0];
      final Map<String, Object> fieldProperties = getClassMapping(parameterType);
      mapping.put("properties", fieldProperties.get("properties"));
    }
    return mapping;
  }

  /**
   * Returns the Lucene field type for the given domain field. <br/>
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
   * @return the corresponding type in the Lucene document stored in Elasticsearch
   * @see <a href="https://www.elastic.co/guide/en/elasticsearch/guide/current/mapping-intro.html">
   *      Elasticsearch Mapping documentation</a>
   */
  public static String getLuceneFieldType(final Class<?> fieldType) {
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
    } else if (fieldType == String.class) {
      return "string";
    } else if (fieldType.isArray()) {
      return getLuceneFieldType(fieldType.getComponentType());
    } else if (Collection.class.isAssignableFrom(fieldType)) {
      return "object";
    }
    throw new MappingException("Unable to retrieve the Lucene field type for " + fieldType);
  }

}
