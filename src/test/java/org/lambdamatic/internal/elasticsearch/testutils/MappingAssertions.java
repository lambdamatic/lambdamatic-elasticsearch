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

/**
 * Custom {@link AbstractAssert} for Elasticsearch mappings.
 */
public class MappingAssertions extends AbstractAssert<MappingAssertions, Map<String, Object>> {

  protected MappingAssertions(final Map<String, Object> actual) {
    super(actual, MappingAssertions.class);
  }

  public static MappingAssertions assertThat(final Map<String, Object> actual) {
    return new MappingAssertions(actual);
  }

  /**
   * Asserts that the actual mapping contains a field with the expected type.
   * 
   * @param fieldName the name of the field to look-up
   * @param expectedType the expected type of the field
   * @return this {@link MappingAssertions} for chaining further assertions
   */
  public MappingAssertions hasMapping(final String fieldName, final String expectedType) {
    hasMapping(fieldName, fieldName, expectedType, actual);
    return this;
  }

  private MappingAssertions hasMapping(final String fullFieldName, final String fieldName,
      final String expectedType, final Map<String, Object> currentMapping) {
    isNotNull();
    final Map<String, Object> fieldMappingProperties =
        (Map<String, Object>) currentMapping.get("properties");
    if (fieldName.contains(".")) {
      final String parentFieldName = fieldName.substring(0, fieldName.indexOf('.'));
      if (fieldMappingProperties.get(parentFieldName) == null) {
        failWithMessage("The actual mapping does not contain the complex field <%s>:\n%s",
            fullFieldName, actual.toString());
      }
      final Map<String, Object> parentFieldMapping =
          (Map<String, Object>) fieldMappingProperties.get(parentFieldName);
      if (parentFieldMapping.get("properties") == null) {
        failWithMessage("The actual mapping for the complex field <%s> has no properties:\n%s",
            fieldName, actual.toString());
      }
      final String childFieldName = fieldName.substring(fieldName.indexOf('.') + 1);
      hasMapping(fullFieldName, childFieldName, expectedType,
          (Map<String, Object>) fieldMappingProperties.get(parentFieldName));
    } else {
      verifyFieldMapping(fullFieldName, fieldName, expectedType, fieldMappingProperties);
    }
    return this;
  }

  private void verifyFieldMapping(final String fullFieldName, final String fieldName,
      final String expectedType, final Map<String, Object> fieldMappingProperties) {
    if (!fieldMappingProperties.containsKey(fieldName)) {
      failWithMessage("The actual mapping does not contain the field <%s>:\n%s", fullFieldName,
          actual.toString());
    }
    final Map<String, Object> fieldMapping =
        (Map<String, Object>) fieldMappingProperties.get(fieldName);
    if (fieldMapping.get("type") == null) {
      failWithMessage("Field <%s> has no simple type (it might be a complex type)", fullFieldName,
          actual, expectedType);
    }
    if (!fieldMapping.get("type").equals(expectedType)) {
      failWithMessage("Field <%s> has not the expected type:<%s> vs <%s>", fullFieldName,
          fieldMapping.get("type"), expectedType);
    }
  }

}
