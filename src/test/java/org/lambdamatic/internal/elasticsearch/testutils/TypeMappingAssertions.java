/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.internal.elasticsearch.testutils;

import java.util.Map;

import org.assertj.core.api.AbstractAssert;
import org.lambdamatic.internal.elasticsearch.clientdsl.responses.GetIndexMappingsResponse.PropertyMapping;
import org.lambdamatic.internal.elasticsearch.clientdsl.responses.GetIndexMappingsResponse.TypeMapping;

/**
 * Custom {@link AbstractAssert} for Elasticsearch mappings.
 */
public class TypeMappingAssertions extends AbstractAssert<TypeMappingAssertions, TypeMapping> {

  protected TypeMappingAssertions(final TypeMapping actual) {
    super(actual, TypeMappingAssertions.class);
  }

  public static TypeMappingAssertions assertThat(final TypeMapping actual) {
    return new TypeMappingAssertions(actual);
  }

  /**
   * Asserts that the actual mapping contains a field with the expected type.
   * 
   * @param fieldName the name of the field to look-up
   * @param expectedType the expected type of the field
   * @return this {@link TypeMappingAssertions} for chaining further assertions
   */
  public TypeMappingAssertions hasMapping(final String fieldName, final String expectedType) {
    hasMapping(fieldName, fieldName, expectedType, actual.getProperties());
    return this;
  }

  private TypeMappingAssertions hasMapping(final String fullFieldName, final String fieldName,
      final String expectedType, Map<String, PropertyMapping> fieldMappingProperties) {
    isNotNull();
    if (fieldName.contains(".")) {
      final String parentFieldName = fieldName.substring(0, fieldName.indexOf('.'));
      if (fieldMappingProperties.get(parentFieldName) == null) {
        failWithMessage("The actual mapping does not contain the complex field <%s>:\n%s",
            fullFieldName, actual.toString());
      }
      final PropertyMapping parentFieldMapping =
          fieldMappingProperties.get(parentFieldName);
      if (parentFieldMapping.isEmpty()) {
        failWithMessage("The actual mapping for the complex field <%s> has no properties:\n%s",
            fieldName, actual.toString());
      }
      final String childFieldName = fieldName.substring(fieldName.indexOf('.') + 1);
      verifyFieldMapping(fullFieldName, childFieldName, expectedType,
          fieldMappingProperties.get(parentFieldName).getProperty(childFieldName));
    } else {
      if (!fieldMappingProperties.containsKey(fieldName)) {
        failWithMessage("The actual mapping does not contain the field <%s>:\n%s", fullFieldName,
            actual.toString());
      }
      verifyFieldMapping(fullFieldName, fieldName, expectedType,
          fieldMappingProperties.get(fieldName));
    }
    return this;
  }

  private void verifyFieldMapping(final String fullFieldName, final String fieldName,
      final String expectedType, final PropertyMapping fieldMapping) {
    if (fieldMapping == null) {
      failWithMessage("The actual mapping does not contain the field <%s>:\n%s", fullFieldName,
          actual.toString());
    }
    if (fieldMapping.getType() == null) {
      failWithMessage("Field <%s> has no simple type (it might be a complex type)", fullFieldName,
          actual, expectedType);
    }
    if (!fieldMapping.getType().equals(expectedType)) {
      failWithMessage("Field <%s> has not the expected type:<%s> vs <%s>", fullFieldName,
          fieldMapping.getType(), expectedType);
    }
  }

}
