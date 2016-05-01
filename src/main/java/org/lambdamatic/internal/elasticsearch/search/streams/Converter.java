/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.internal.elasticsearch.search.streams;

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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.elasticsearch.search.SearchHit;
import org.lambdamatic.elasticsearch.annotations.DocumentField;
import org.lambdamatic.elasticsearch.annotations.DocumentId;
import org.lambdamatic.elasticsearch.exceptions.ConversionException;

/**
 * Converter utility to convert elements contained in a returned {@link SearchHit} into Domain
 * objects.
 */
public class Converter {

  /**
   * Converts the elements contained in the given {@code searchHit} into a Domain instance.
   * 
   * @param domainType The domain type associated with this index and type.
   * @param documentId the id of the document retrieved in Elasticsearch
   * @param documentSource the source of the document retrieved in Elasticsearch
   * @return the generated instance of DomainType
   * @throws ConversionException if the conversion of the given {@code searchHit} into an instance
   *         of the given {@code domainType} failed.
   */
  public static <DomainType> DomainType toDomainType(final Class<DomainType> domainType,
      final String documentId, final Map<String, Object> documentSource) {
    try {
      final DomainType domainObject = toDomainType(domainType, documentSource);
      setDomainObjectId(domainObject, documentId);
      return domainObject;
    } catch (SecurityException | IllegalArgumentException | IntrospectionException e) {
      throw new ConversionException("Failed to convert a searchHit into a domain object", e);
    }
  }

  /**
   * Converts the elements contained in the given {@code searchHit} into a Domain instance.
   * 
   * @param domainType The domain type associated with this index and type.
   * @param documentSource the source of the document retrieved in Elasticsearch
   * @return the generated instance of DomainType
   * @throws ConversionException if the conversion of the given {@code searchHit} into an instance
   *         of the given {@code domainType} failed.
   */
  // FIXME: need to support type hierarchy
  private static <D> D toDomainType(final Class<D> domainType,
      final Map<String, Object> documentSource) {
    try {
      final Constructor<D> domainTypeConstructor = domainType.getConstructor();
      final D domainObject = domainTypeConstructor.newInstance();
      final BeanInfo domainTypeInfo = Introspector.getBeanInfo(domainType);
      Stream.of(domainTypeInfo.getPropertyDescriptors())
          .forEach(propertyDescriptor -> setDomainObjectProperty(domainObject, propertyDescriptor,
              documentSource));
      return domainObject;
    } catch (NoSuchMethodException | SecurityException | InstantiationException
        | IllegalAccessException | IllegalArgumentException | InvocationTargetException
        | IntrospectionException e) {
      throw new ConversionException("Failed to convert a searchHit into a domain object", e);
    }
  }

  /**
   * Sets the given {@code documentId} value to the given {@code domainObject} using the setter for
   * the property annotated with the {@link DocumentField} annotation and having the
   * {@link DocumentField#id()} flag set to <code>true</code>.
   * 
   * @param domainObject the instance of DomainType on which to set the {@code id} property
   * @param domainTypeInfo the associated {@link BeanInfo} to find the {@code id} property
   * @param documentId the value of the document id.
   * @throws IntrospectionException if introspection of the given DomainType failed.
   */
  private static <DomainType> void setDomainObjectId(final DomainType domainObject,
      final String documentId) throws IntrospectionException {
    final BeanInfo domainTypeInfo = Introspector.getBeanInfo(domainObject.getClass());
    final Field idField = Stream.of(domainObject.getClass().getDeclaredFields())
        .filter(field -> field.getAnnotation(DocumentId.class) != null).findFirst()
        .orElseThrow(() -> new ConversionException(
            "No id document field declared on type '" + domainObject.getClass().getName() + "'"));
    final PropertyDescriptor idPropertyDescriptor =
        Stream.of(domainTypeInfo.getPropertyDescriptors())
            .filter(propertyDescriptor -> propertyDescriptor.getName().equals(idField.getName()))
            .findFirst().orElseThrow(
                () -> new ConversionException("Unable to find property descriptor for field '"
                    + idField.getName() + "' in type '" + domainObject.getClass().getName() + "'"));
    try {
      idPropertyDescriptor.getWriteMethod().invoke(domainObject,
          convertValue(documentId, idField.getType()));
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw new ConversionException(
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
  private static Object convertValue(final Object value, final Class<?> targetType) {
    if (targetType.isAssignableFrom(value.getClass())) {
      return value;
    } else if (targetType.isPrimitive()) {
      if (targetType == int.class) {
        return Integer.parseInt(value.toString());
      }
      // FIXME: implement same logic for double, short, long, boolean, byte, float
      throw new ConversionException("Failed to convert value '" + value.toString() + "' ("
          + value.getClass().getName() + ")  into a " + targetType.getName()
          + ": no object to primitive conversion available.");
    }
    try {
      final Method convertMethod = getConvertMethod(targetType);
      if (convertMethod != null) {
        return convertMethod.invoke(null, value.toString());
      }
      throw new ConversionException(
          "Failed to convert value '" + value.toString() + "' (" + value.getClass().getName()
              + ")  into a " + targetType.getName() + ": no conversion method available.");
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
        | SecurityException e) {
      throw new ConversionException("Failed to convert value '" + value.toString() + "' ("
          + value.getClass().getName() + ") into a " + targetType.getClass().getName(), e);
    }
  }

  /**
   * Attempts to look-up a {@code valueOf(String)} or {@code parse(CharSequence)} method in the
   * given {@code type} to allow for value setting with a conversion.
   * 
   * @param type
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
   * Sets the Domain Type instance property from the given descriptor
   * 
   * @param domainObject the instance of DomainType on which to set the entry in the given
   *        {@code searchHitElements} corresponding to the given {@code propertyDescriptor} (unless
   *        it is <code>null</code>).
   * @param propertyDescriptor the {@link PropertyDescriptor} to use to set the domain type
   *        property.
   * @param documentElements the elements contained in document returned by Elasticsearch.
   * @throws SecurityException
   * @throws NoSuchFieldException
   * @throws ConversionException if setting the searchHit value on the domain type property failed.
   */
  @SuppressWarnings("unchecked")
  private static <D> void setDomainObjectProperty(final D domainObject,
      final PropertyDescriptor propertyDescriptor, final Map<String, Object> documentElements) {
    final Object documentElementValue = documentElements.get(propertyDescriptor.getName());
    try {
      if (documentElementValue != null) {
        // support for Lists
        if (List.class.isAssignableFrom(propertyDescriptor.getPropertyType())) {
          final Type targetType = ((ParameterizedType) domainObject.getClass()
              .getDeclaredField(propertyDescriptor.getName()).getGenericType())
                  .getActualTypeArguments()[0];
          // casting to Class<D> fixes a compilation error on Travis-ci (running JDK 1.8_031) 
          // but might have side-effects (ClassCastExceptions ?) when type inheritance will have to 
          // be supported
          final Class<D> targetClass = (Class<D>) Class.forName(targetType.getTypeName());
          final List<?> nestedElements = ((List<Map<String, Object>>) documentElementValue).stream()
              .map(element -> toDomainType(targetClass, element)).collect(Collectors.toList());
          propertyDescriptor.getWriteMethod().invoke(domainObject, nestedElements);
        }
        // singular elements
        else {
          propertyDescriptor.getWriteMethod().invoke(domainObject,
              convertValue(documentElementValue, propertyDescriptor.getPropertyType()));
        }
      }
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
        | ClassNotFoundException | NoSuchFieldException | SecurityException e) {
      throw new ConversionException("Failed to set value '" + documentElementValue
          + "' on property '" + propertyDescriptor.getName() + "' of domain type '"
          + domainObject.getClass().getName() + "'", e);
    }
  }

}
