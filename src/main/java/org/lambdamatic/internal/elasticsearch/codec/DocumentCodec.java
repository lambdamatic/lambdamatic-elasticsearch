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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

/**
 * Converter utility to map elements contained in a returned {@link SearchHit} into domain objects,
 * or to convert domain objects into source maps.
 * 
 * @param <D> the type to encode/decode
 */
public class DocumentCodec<D> {

  /** The domain type to encode/decode. */
  private final Class<D> domainType;

  /**
   * The {@link BeanInfo} associated with the domainType.
   */
  private final BeanInfo domainTypeBeanInfo;

  /**
   * Constructor.
   * 
   * @param domainType The domain type to encode/decode. Must be annotated with {@link Document} or
   *        {@link EmbeddedDocument}.
   * @throws DomainTypeException if an exception occurs during introspection of the given domain
   *         type or if the given {@code domainType} is neither annotated with {@link Document} nor
   *         {@link EmbeddedDocument}.
   */
  public DocumentCodec(final Class<D> domainType) throws DomainTypeException {
    this.domainType = domainType;
    // check that the given type
    if (domainType.getAnnotationsByType(Document.class) == null
        || domainType.getAnnotationsByType(EmbeddedDocument.class) == null) {
      throw new DomainTypeException("Domain type '" + domainType + "' must be annotated with '"
          + Document.class.getName() + "' or '" + EmbeddedDocument.class.getName() + "'");
    }
    try {
      this.domainTypeBeanInfo = Introspector.getBeanInfo(domainType);
    } catch (IntrospectionException e) {
      throw new DomainTypeException("Failed to analyse domain type '" + domainType.getName() + "'",
          e);
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
  @SuppressWarnings("unchecked")
  public D toDomainType(final String documentId, final Map<String, Object> documentSource) {
    try {
      final D domainObject = (D) toDomainType(this.domainType, documentSource);
      setDomainObjectId(domainObject, documentId);
      return domainObject;
    } catch (SecurityException | IllegalArgumentException e) {
      throw new CodecException("Failed to convert a searchHit into a domain object", e);
    }
  }

  /**
   * Converts the elements contained in the given {@code searchHit} into a Domain instance.
   * 
   * @param documentSource the source of the document retrieved in Elasticsearch
   * @return the generated instance of DomainType
   * @throws CodecException if the conversion of the given {@code searchHit} into an instance of the
   *         given {@code domainType} failed.
   */
  // FIXME: need to support type hierarchy
  private Object toDomainType(final Class<?> domainType, final Map<String, Object> documentSource) {
    try {
      final Constructor<?> domainTypeConstructor = domainType.getConstructor();
      final Object domainObject = domainTypeConstructor.newInstance();
      Stream.of(domainTypeBeanInfo.getPropertyDescriptors())
          .forEach(propertyDescriptor -> setDomainObjectProperty(domainObject, propertyDescriptor,
              documentSource));
      return domainObject;
    } catch (NoSuchMethodException | SecurityException | InstantiationException
        | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw new CodecException("Failed to convert a documentSource into a domain object of type '"
          + domainType.getName() + "'", e);
    }
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
  public String getDomainObjectId(final D domainObject) {
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
  public void setDomainObjectId(final D domainObject, final String documentId) {
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

  private PropertyDescriptor getIdPropertyDescriptor(final D domainObject, final Field idField) {
    return Stream.of(this.domainTypeBeanInfo.getPropertyDescriptors())
        .filter(propertyDescriptor -> propertyDescriptor.getName().equals(idField.getName()))
        .findFirst()
        .orElseThrow(() -> new CodecException("Unable to find property descriptor for field '"
            + idField.getName() + "' in type '" + domainObject.getClass().getName() + "'"));
  }

  /**
   * Sets the Domain Type instance property from the given descriptor
   * 
   * @param domainObject the instance of DomainType on which to set the entry in the given
   *        {@code searchHitElements} corresponding to the given {@code propertyDescriptor} (unless
   *        it is <code>null</code>).
   * @param propertyDescriptor the {@link PropertyDescriptor} to use to set the domain type
   *        property.
   * @param documentElements the elements contained in document returned by Elasticsearch.
   * @throws CodecException if setting the searchHit value on the domain type property failed.
   */
  @SuppressWarnings("unchecked")
  private static void setDomainObjectProperty(final Object domainObject,
      final PropertyDescriptor propertyDescriptor, final Map<String, Object> documentElements) {
    final Object documentElementValue = documentElements.get(propertyDescriptor.getName());
    try {
      if (documentElementValue != null) {
        if (List.class.isAssignableFrom(propertyDescriptor.getPropertyType())) {
          // support for Lists
          final Type targetType = ((ParameterizedType) domainObject.getClass()
              .getDeclaredField(propertyDescriptor.getName()).getGenericType())
                  .getActualTypeArguments()[0];
          final Class<?> targetClass = Class.forName(targetType.getTypeName());
          final DocumentCodec<?> documentElementCodec = new DocumentCodec<>(targetClass);
          final List<?> nestedElements = ((List<Map<String, Object>>) documentElementValue).stream()
              .map(element -> documentElementCodec.toDomainType(targetClass, element))
              .collect(Collectors.toList());
          propertyDescriptor.getWriteMethod().invoke(domainObject, nestedElements);
        } else {
          // singular elements
          propertyDescriptor.getWriteMethod().invoke(domainObject,
              convertValue(documentElementValue, propertyDescriptor.getPropertyType()));
        }
      }
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
        | ClassNotFoundException | NoSuchFieldException | SecurityException e) {
      throw new CodecException("Failed to set value '" + documentElementValue + "' on property '"
          + propertyDescriptor.getName() + "' of domain type '" + domainObject.getClass().getName()
          + "'", e);
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
  private static Object convertValue(final Object value, final Class<?> targetType) {
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
  public Map<String, Object> toSourceMap(final Object domainObject) {
    if (domainObject == null) {
      return null;
    }
    final Map<String, Object> sourceMap = new HashMap<>();
    // TODO: also include fields from superclasses
    Stream.of(this.domainType.getDeclaredFields())
        .map(domainField -> new Pair<>(domainField, domainField.getAnnotation(DocumentField.class)))
        .filter(pair -> pair.getRight() != null).forEach(pair -> {
          final Field domainField = pair.getLeft();
          final DocumentField domainFieldAnnotation = pair.getRight();
          final String documentFieldName = getDocumentFieldName(domainField, domainFieldAnnotation);
          final Object sourceMapValue = toSourceMapValue(domainObject, domainField);
          sourceMap.put(documentFieldName, sourceMapValue);
        });
    return sourceMap;
  }

  private Object toSourceMapValue(final Object document, final Field domainField) {
    if (Collection.class.isAssignableFrom(domainField.getType())) {
      // collection (list, set)
      // array
      @SuppressWarnings("unchecked")
      final List<Object> domainFieldValue =
          (List<Object>) getDocumentFieldValue(document, domainField, this.domainTypeBeanInfo);
      if (domainFieldValue != null) {
        return domainFieldValue.stream().map(v -> {
          return new DocumentCodec<>(v.getClass()).toSourceMap(v);
        }).collect(Collectors.toCollection(() -> initCollection(domainField)));
      }
      return null;
    } else if (Map.class.isAssignableFrom(domainField.getType())) {
      // map
      throw new CodecException("Domain type fields of type 'java.util.Map' are not supported yet.");
    } else if (domainField.getType().isArray()) {
      // array
      final Object[] domainFieldValue =
          (Object[]) getDocumentFieldValue(document, domainField, this.domainTypeBeanInfo);
      if (domainFieldValue != null && domainFieldValue.length > 0) {
        // final Object documentValue =
        // Array.newInstance(domainField.getType(), domainFieldValue.length);
        // Stream.of(domainFieldValue).forEach(v -> Array.set(documentValue, index, value););
        // FIXME: finish implementation
        throw new CodecException("Domain type fields as arrays are not supported yet.");
      }
      return null;
      // return documentValue;
    } else {
      // singular field
      return getDocumentFieldValue(document, domainField, this.domainTypeBeanInfo);
    }
  }

  private static Collection<Object> initCollection(final Field domainField) {
    if (List.class.equals(domainField.getType())) {
      return new ArrayList<>();
    } else if (Set.class.equals(domainField.getType())) {
      return new HashSet<>();
    }
    throw new CodecException("Cannot instanciate a collection from the given field type: '"
        + domainField.getType().getName() + ". Field '" + domainField.getDeclaringClass().getName()
        + "." + domainField.getName() + "' should be of type 'java.util.Set' or 'java.util.List'.");
  }

  private static <D> Object getDocumentFieldValue(final D document, final Field field,
      final BeanInfo domainTypeBeanInfo) {
    final PropertyDescriptor propertyDescriptor = getPropertyDescriptor(domainTypeBeanInfo, field);
    try {
      return propertyDescriptor.getReadMethod().invoke(document, new Object[0]);
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw new CodecException("Cannot invoke getter for field named '" + field.getName()
          + "' in class '" + document.getClass().getName() + "'", e);
    }
  }

  /**
   * Locates the {@link PropertyDescriptor} for the given {@code field} in the given
   * {@code domainTypeBeanInfo}.
   * 
   * @param domainTypeBeanInfo the {@link BeanInfo} for the domain type
   * @param field the field whose {@link PropertyDescriptor} needs to be found
   * @return the corresponding {@link PropertyDescriptor}
   * @throws CodecException if the {@link PropertyDescriptor} could not be located
   */
  private static <D> PropertyDescriptor getPropertyDescriptor(final BeanInfo domainTypeBeanInfo,
      final Field field) {
    final PropertyDescriptor propertyDescriptor =
        Stream.of(domainTypeBeanInfo.getPropertyDescriptors())
            .filter(p -> p.getName().equals(field.getName())).findFirst()
            .orElseThrow(() -> new CodecException("Cannot locate getter for field named '"
                + field.getName() + "' in class '" + field.getDeclaringClass().getName() + "'"));
    return propertyDescriptor;
  }

  /**
   * Gets the name of the Elasticsearch/Lucene field associated with the given {@code documentField}
   * annotated with the given {@code documentFieldAnnotation}.
   * 
   * @param domainField the {@link Field} in the domain type
   * @param domainFieldAnnotation the associated {@link DocumentField} annotation
   * @return the name of the field in the Elasticsearch/Lucene document
   */
  private static String getDocumentFieldName(final Field domainField,
      final DocumentField domainFieldAnnotation) {
    if (domainFieldAnnotation != null && domainFieldAnnotation.name() != null
        && !domainFieldAnnotation.name().isEmpty()) {
      return domainFieldAnnotation.name();
    }
    return domainField.getName();
  }

}
