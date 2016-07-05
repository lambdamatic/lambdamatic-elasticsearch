/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.internal.elasticsearch.codec;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.elasticsearch.search.SearchHit;
import org.lambdamatic.elasticsearch.annotations.Document;
import org.lambdamatic.elasticsearch.annotations.DocumentField;
import org.lambdamatic.elasticsearch.annotations.DocumentId;
import org.lambdamatic.elasticsearch.annotations.EmbeddedDocument;
import org.lambdamatic.elasticsearch.exceptions.CodecException;
import org.lambdamatic.elasticsearch.exceptions.DomainTypeException;
import org.lambdamatic.elasticsearch.utils.Pair;
import org.lambdamatic.internal.elasticsearch.MappingException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Converter utility to map elements contained in a returned {@link SearchHit} into domain objects,
 * or to convert domain objects into source maps.
 * 
 * @param <T> the type to encode/decode
 */
public class DocumentCodec<T> {

  /** The domain type to encode/decode. */
  final Class<T> domainType;

  /**
   * The {@link ObjectMapper} to encode/decode documents.
   */
  private final ObjectMapper objectMapper;

  /**
   * The {@link BeanInfo} associated with the domainType.
   */
  final BeanInfo domainTypeBeanInfo;

  /**
   * Constructor.
   * 
   * @param domainType The domain type to encode/decode. Must be annotated with {@link Document} or
   *        {@link EmbeddedDocument}
   * @param objectMapper the {@link ObjectMapper} to encode/decode documents
   * @throws DomainTypeException if an exception occurs during introspection of the given domain
   *         type.
   */
  public DocumentCodec(final Class<T> domainType, final ObjectMapper objectMapper)
      throws DomainTypeException {
    this.domainType = domainType;
    this.objectMapper = objectMapper;
    try {
      this.domainTypeBeanInfo = Introspector.getBeanInfo(domainType);
    } catch (IntrospectionException e) {
      throw new DomainTypeException("Failed to analyse domain type '" + domainType.getName() + "'",
          e);
    }
  }

  /**
   * Converts the given {@code document} into a source map, to be sent to Elasticsearch.
   * <p>
   * <strong>Note</strong>: the resulting source map does not include the {@code id} field, which
   * should be separately retrieved using the {@link DocumentCodec#getDomainObjectId(Object)}
   * </p>
   * 
   * @param domainObject the document to convert
   * @return the corresponding source map, or <code>null</code> if the given {@code document} was
   *         <code>null</code>, too.
   * 
   */
  public String encode(final Object domainObject) {
    try {
      return this.objectMapper.writeValueAsString(domainObject);
    } catch (JsonProcessingException e) {
      throw new CodecException("Failed to convert domain object of type '"
          + this.domainType.getName() + "' into a document source", e);
    }
    
  }

  /**
   * Converts the elements contained in the given {@code searchHit} into a Domain instance.
   * 
   * @param documentId the id of the document retrieved in Elasticsearch
   * @param documentSource the source of the document retrieved in Elasticsearch
   * @return the generated instance of DomainType
   * @throws CodecException if the conversion of the given {@code searchHit} into an instance of the
   *         given {@code domainType} failed.
   */
  public T decode(final String documentId, final String documentSource) {
    final T domainObject;
    try {
      domainObject = this.objectMapper.readValue(documentSource, this.domainType);
    } catch (IOException e) {
      throw new CodecException("Failed to convert a document source into a domain object of type '"
          + this.domainType.getName() + "'", e);
    }
    setDomainObjectId(domainObject, documentId);
    return domainObject;

  }

  /**
   * Gets the {@code documentId} value for the given {@code domainObject} using the getter for the
   * property annotated with the {@link DocumentField} annotation and having the
   * {@link DocumentField#id()} flag set to <code>true</code>.
   * 
   * @param domainObject the instance of DomainType on which to set the {@code id} property
   * @return the {@code documentId} converted as a String, or <code>null</code> if none was found
   * @throws IntrospectionException if introspection of the given DomainType failed.
   */
  public String getDomainObjectId(final T domainObject) {
    final Field idField = getIdField(domainObject.getClass());
    final PropertyDescriptor idPropertyDescriptor = getIdPropertyDescriptor(domainObject, idField);
    try {
      final Object documentId = idPropertyDescriptor.getReadMethod().invoke(domainObject);
      return documentId != null ? documentId.toString() : null;
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw new CodecException(
          "Failed to get Id value for document of type '" + domainObject.getClass().getName()
              + ") using method '" + idPropertyDescriptor.getReadMethod().toString() + "'");
    }
  }

  /**
   * Sets the given {@code documentId} value to the given {@code domainObject} using the setter for
   * the property annotated with the {@link DocumentField} annotation and having the
   * {@link DocumentField#id()} flag set to <code>true</code>.
   * 
   * @param domainObject the instance of DomainType on which to set the {@code id} property
   * @param documentId the value of the document id.
   */
  public void setDomainObjectId(final T domainObject, final String documentId) {
    final Field idField = getIdField(domainObject.getClass());
    final PropertyDescriptor idPropertyDescriptor = getIdPropertyDescriptor(domainObject, idField);
    try {
      idPropertyDescriptor.getWriteMethod().invoke(domainObject,
          convertValue(documentId, idField.getType()));
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw new CodecException(
          "Failed to set value '" + documentId + "' (" + documentId.getClass().getName()
              + ") using method '" + idPropertyDescriptor.getWriteMethod().toString() + "'");
    }
  }

  /**
   * Converts the given {@code value} into a value of type {@code targetType}.
   * <p>
   * <strong>Note:</strong>The conversion relies on the existence of a static
   * {@code valueOf(String)} method in the given {@code targetType} to convert the value.
   * </p>
   * 
   * @param value the input value
   * @param targetType the target type of the value to return
   * @return the converted value (or the value itself if no conversion was necessary)
   */
  protected static Object convertValue(final Object value, final Class<?> targetType) {
    if (targetType.isAssignableFrom(value.getClass())) {
      return value;
    } else if (targetType.isPrimitive()) {
      if (targetType == boolean.class) {
        return Boolean.parseBoolean(value.toString());
      } else if (targetType == byte.class) {
        return Byte.parseByte(value.toString());
      } else if (targetType == short.class) {
        return Short.parseShort(value.toString());
      } else if (targetType == int.class) {
        return Integer.parseInt(value.toString());
      } else if (targetType == long.class) {
        return Long.parseLong(value.toString());
      } else if (targetType == double.class) {
        return Double.parseDouble(value.toString());
      } else if (targetType == float.class) {
        return Float.parseFloat(value.toString());
      }
      throw new CodecException("Failed to convert value '" + value.toString() + "' ("
          + value.getClass().getName() + ")  into a " + targetType.getName()
          + ": no object to primitive conversion available.");
    }
    try {
      final Method convertMethod = getConvertMethod(targetType);
      if (convertMethod != null) {
        return convertMethod.invoke(null, value.toString());
      }
      throw new CodecException(
          "Failed to convert value '" + value.toString() + "' (" + value.getClass().getName()
              + ")  into a " + targetType.getName() + ": no conversion method available.");
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
        | SecurityException e) {
      throw new CodecException("Failed to convert value '" + value.toString() + "' ("
          + value.getClass().getName() + ") into a " + targetType.getClass().getName(), e);
    }
  }

  /**
   * Attempts to look-up a {@code valueOf(String)} or {@code parse(CharSequence)} method in the
   * given {@code targetType} to allow for value setting with a conversion.
   * 
   * @param targetType the type in which the convert method must be looked-up
   * @return the method to apply, or <code>null</code> if none was found.
   */
  private static Method getConvertMethod(final Class<?> targetType) {
    try {
      return targetType.getMethod("valueOf", String.class);
    } catch (NoSuchMethodException | SecurityException e) {
      // ignore, the method just does not exist.
    }
    try {
      return targetType.getMethod("parse", CharSequence.class);
    } catch (NoSuchMethodException | SecurityException e) {
      // ignore, the method just does not exist.
    }
    return null;
  }

  /**
   * Analyze the given domain type and returns the name of its field to use as the document id, if
   * available.
   * 
   * @param domainType the domain type to analyze
   * @return the <strong>single</strong> Java field annotated with {@link DocumentId}. If no field
   *         matches the criteria or more than one field is matches these criteria, a
   *         {@link MappingException} is thrown.
   */
  public static Field getIdField(final Class<?> domainType) {
    final List<Pair<Field, DocumentId>> candidateFields = Stream.of(domainType.getDeclaredFields())
        .map(field -> new Pair<>(field, field.getAnnotation(DocumentId.class)))
        .filter(pair -> pair.getRight() != null).collect(Collectors.toList());
    if (candidateFields.isEmpty()) {
      throw new MappingException("No field is annotated with @{} in type {}",
          DocumentId.class.getName(), domainType);
    } else if (candidateFields.size() > 1) {
      final String fieldNames = candidateFields.stream().map(pair -> pair.getLeft().getName())
          .collect(Collectors.joining(", "));
      throw new MappingException("More than one field is annotated with @{} in type {}: {}",
          DocumentId.class.getName(), domainType, fieldNames);
    }
    return candidateFields.get(0).getLeft();
  }

  private PropertyDescriptor getIdPropertyDescriptor(final T domainObject, final Field idField) {
    return Stream.of(this.domainTypeBeanInfo.getPropertyDescriptors())
        .filter(propertyDescriptor -> propertyDescriptor.getName().equals(idField.getName()))
        .findFirst()
        .orElseThrow(() -> new CodecException("Unable to find property descriptor for field '"
            + idField.getName() + "' in type '" + domainObject.getClass().getName() + "'"));
  }

}
