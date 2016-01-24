/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.internal.elasticsearch;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.lambdamatic.elasticsearch.annotations.DocumentField;

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
   * Analyze the given domain type and returns the name of the Lucene document field to use as the
   * document id, if available.
   * 
   * @param domainType the domain type to analyze
   * @return the value of {@link DocumentField#name()} is present or the actual {@link Field} name
   *         if there is a single Field annotated with {@link DocumentField} whose
   *         {@link DocumentField#id()} value is set to <code>true</code>. If no field matches the
   *         criteria, <code>null</code> is returned. If more than one field is matches these
   *         criteria, a {@link MappingException} is thrown.
   */
  public static String getIdFieldName(Class<?> domainType) {
    final List<ImmutablePair<Field, DocumentField>> candidateFields =
        Stream.of(domainType.getDeclaredFields())
            .map(field -> ImmutablePair.of(field, field.getAnnotation(DocumentField.class)))
            .filter(pair -> pair.right != null && pair.right.id()).collect(Collectors.toList());
    if (candidateFields.isEmpty()) {
      return null;
    } else if (candidateFields.size() > 1) {
      final String fieldNames = candidateFields.stream().map(pair -> pair.left.getName())
          .collect(Collectors.joining(", "));
      throw new MappingException(
          "More than one field is annotated with '@StringField(id=true)': {}", fieldNames);
    }
    final Field domainField = candidateFields.get(0).left;
    final DocumentField domainFieldAnnotation = candidateFields.get(0).right;
    if (domainFieldAnnotation.name() == null || domainFieldAnnotation.name().isEmpty()) {
      return domainField.getName();
    }
    return domainFieldAnnotation.name();
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
    } else if (fieldType == Date.class) {
      return "date";
    } else if (fieldType == String.class) {
      return "string";
    }
    throw new MappingException("Unable to retrieve the Lucene field type for " + fieldType);
  }

}
