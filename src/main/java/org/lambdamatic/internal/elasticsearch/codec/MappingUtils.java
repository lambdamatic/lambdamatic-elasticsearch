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
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.lambdamatic.elasticsearch.annotations.Document;
import org.lambdamatic.elasticsearch.annotations.DocumentField;
import org.lambdamatic.elasticsearch.annotations.DocumentId;
import org.lambdamatic.elasticsearch.annotations.Latitude;
import org.lambdamatic.elasticsearch.annotations.Longitude;
import org.lambdamatic.elasticsearch.exceptions.CodecException;
import org.lambdamatic.elasticsearch.exceptions.DomainTypeException;

/**
 * Utility class to retrieve the mapping of domain types.
 */
public class MappingUtils {

  /**
   * Looks-up the single field in the given {@code domainType} that is annotated with the given
   * {@code annotationClass}.
   * 
   * @param domainType the domain type to inspect
   * @param annotationClass the annotation on the field to find
   * @return the matching field
   * @throws CodecException if no field was annotated with the given {@code annotationClass} or if
   *         more than one field had this annotation.
   */
  public static Field getSingleFieldAnnotatedWith(final Class<?> domainType,
      final Class<? extends Annotation> annotationClass) {
    final Field[] fields = domainType.getDeclaredFields();
    final List<Field> annotatedFields = Stream.of(fields)
        .filter(field -> field.getAnnotation(annotationClass) != null).collect(Collectors.toList());
    if (annotatedFields.isEmpty()) {
      throw new CodecException("No field annotated with '@" + annotationClass.getName()
          + "' could be found in domain class '" + domainType.getName() + "'");
    } else if (annotatedFields.size() > 1) {
      throw new CodecException("Too many fields annotated with '@" + annotationClass.getName()
          + "' in domain class '" + domainType.getName() + "': " + annotatedFields.toString());
    }
    return annotatedFields.get(0);
  }


  /**
   * Sets the given {@code targetValue} to the given {@code targetField} on the given
   * {@code domainObject}.
   * 
   * @param domainObject the domain object on which the field will be set
   * @param propertyDescriptor the {@link PropertyDescriptor} of the target field to set
   * @param targetValue the value to set on the target field
   */
  public static <T> void setFieldValue(final T domainObject,
      final PropertyDescriptor propertyDescriptor, final Object targetValue) {
    try {
      propertyDescriptor.getWriteMethod().invoke(domainObject, targetValue);
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw new CodecException("Cannot invoke setter for field named '"
          + propertyDescriptor.getName() + "' in class '" + domainObject.getClass().getName() + "'",
          e);
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
  static <D> PropertyDescriptor getPropertyDescriptor(final BeanInfo domainTypeBeanInfo,
      final Field field) {
    final PropertyDescriptor propertyDescriptor =
        Stream.of(domainTypeBeanInfo.getPropertyDescriptors())
            .filter(p -> p.getName().equals(field.getName())).findFirst()
            .orElseThrow(() -> new CodecException("Cannot locate getter for field named '"
                + field.getName() + "' in class '" + field.getDeclaringClass().getName() + "'"));
    return propertyDescriptor;
  }


  /**
   * Finds the name of the Elasticsearch fields corresponding to the domain type fields annotated
   * with {@link DocumentField} or {@link DocumentId}.
   * 
   * @param domainType the domain type to analyze.
   * @return a {@link Map} containing the the Elasticsearch field name (key) associated with the
   *         {@link PropertyDescriptor} of the corresponding field in the domain type (value)
   */
  // TODO: [inheritance] also include fields from the superclasses
  public static Map<String, FieldDescriptor> findMappings(final Class<?> domainType) {
    final BeanInfo domainTypeBeanInfo;
    try {
      domainTypeBeanInfo = Introspector.getBeanInfo(domainType);
    } catch (IntrospectionException e) {
      throw new DomainTypeException("Failed to analyse domain type '" + domainType.getName() + "'",
          e);
    }
    final Map<String, FieldDescriptor> mappings = new HashMap<>();
    // mandatory, single field annotated with @DocumentId if the given type is annotated with @Document
    if (domainType.isAnnotationPresent(Document.class)) {
      final List<Field> documentIdFields = Stream.of(domainType.getDeclaredFields())
          .filter(domainField -> domainField.isAnnotationPresent(DocumentId.class))
          .collect(Collectors.toList());
      if (documentIdFields.isEmpty()) {
        throw new CodecException("Domain type '" + domainType.getName()
            + "' is missing a field annotated with @" + DocumentId.class.getName() + "'");
      } else if (documentIdFields.size() > 1) {
        throw new CodecException("Domain type '" + domainType.getName()
            + "' has more than one field annotated with @" + DocumentId.class.getName() + "'");
      }
      final PropertyDescriptor propertyDescriptor =
          getPropertyDescriptor(domainTypeBeanInfo, documentIdFields.get(0));
      mappings.put(DocumentId.ID_FIELD_NAME,
          new FieldDescriptor(documentIdFields.get(0), propertyDescriptor));
    }
    // other fields annotated with @DocumentField
    Stream.of(domainType.getDeclaredFields())
        .filter(domainField -> domainField.isAnnotationPresent(DocumentField.class))
        .forEach(domainField -> {
          final DocumentField domainFieldAnnotation =
              domainField.getAnnotation(DocumentField.class);
          final String documentFieldName = getDocumentFieldName(domainField, domainFieldAnnotation);
          final PropertyDescriptor propertyDescriptor =
              getPropertyDescriptor(domainTypeBeanInfo, domainField);
          mappings.put(documentFieldName, new FieldDescriptor(domainField, propertyDescriptor));
        });
    return mappings;
  }

  /**
   * Finds the name of the Elasticsearch fields corresponding to the latitude and longitude fields
   * annotated with {@link Latitude} or {@link Longitude}.
   * 
   * @param domainType the domain type to analyze.
   * @return a {@link Map} containing the the Elasticsearch field name (key) associated with the
   *         {@link PropertyDescriptor} of the corresponding field in the domain type (value)
   */
  // TODO: [inheritance] also include fields from the superclasses
  public static Map<String, FieldDescriptor> findLocationMappings(final Class<?> domainType) {
    final BeanInfo domainTypeBeanInfo;
    try {
      domainTypeBeanInfo = Introspector.getBeanInfo(domainType);
    } catch (IntrospectionException e) {
      throw new DomainTypeException("Failed to analyse domain type '" + domainType.getName() + "'",
          e);
    }
    final Map<String, FieldDescriptor> mappings = new HashMap<>();
    Stream.of(domainType.getDeclaredFields())
        .filter(domainField -> domainField.isAnnotationPresent(Latitude.class)).findFirst()
        .ifPresent(latitudeField -> {
          final PropertyDescriptor propertyDescriptor =
              getPropertyDescriptor(domainTypeBeanInfo, latitudeField);
          mappings.put("lat", new FieldDescriptor(latitudeField, propertyDescriptor));
        });
    Stream.of(domainType.getDeclaredFields())
        .filter(domainField -> domainField.isAnnotationPresent(Longitude.class)).findFirst()
        .ifPresent(longitudeField -> {
          final PropertyDescriptor propertyDescriptor =
              getPropertyDescriptor(domainTypeBeanInfo, longitudeField);
          mappings.put("lon", new FieldDescriptor(longitudeField, propertyDescriptor));
        });
    return mappings;
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

  /**
   * A Tuple to hold a {@link Field} along with its associated {@link PropertyDescriptor}.
   */
  public static class FieldDescriptor {

    /**
     * The field.
     */
    private final Field field;

    /**
     * The {@link PropertyDescriptor} associated to the given {@code field}.
     */
    private final PropertyDescriptor propertyDescriptor;

    /**
     * Constructor.
     * 
     * @param field the field
     * @param propertyDescriptor the {@link PropertyDescriptor} associated to the given
     *        {@code field}
     */
    public FieldDescriptor(final Field field, final PropertyDescriptor propertyDescriptor) {
      this.field = field;
      this.propertyDescriptor = propertyDescriptor;
    }

    /**
     * @return the field.
     */
    public Field getField() {
      return this.field;
    }

    /**
     * @return the {@link PropertyDescriptor} associated to the given {@code field}.
     */
    public PropertyDescriptor getPropertyDescriptor() {
      return this.propertyDescriptor;
    }

  }

}
