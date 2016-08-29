/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.elasticsearch.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify that the document field of type {@link String} is an exact value, and can
 * be used for filtering, sorting and aggregations.
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Keyword {

  /** Mapping field-level query time boosting. Accepts a floating point number, defaults to 1.0. */
  float boost() default 1.0f;

  /**
   * Should the field be stored on disk in a column-stride fashion, so that it can later be used for
   * sorting, aggregations, or scripting? Accepts true (default) or false.
   */
  boolean doc_values() default true;

  /**
   * Should global ordinals be loaded eagerly on refresh? Accepts true or false (default). Enabling
   * this is a good idea on fields that are frequently used for terms aggregations.
   */
  boolean eager_global_ordinals() default false;

  /**
   * Do not index any string longer than this value. Defaults to 2147483647 so that all values would
   * be accepted.
   */
  int ignore_above() default 2147483647;

  /**
   * Whether or not the field value should be included in the _all field? Accepts true or false.
   * Defaults to false if index is set to no, or if a parent object field sets include_in_all to
   * false. Otherwise defaults to true.
   */
  boolean include_in_all() default true;

  /** Should the field be searchable? Accepts true (default) or false. */
  boolean index() default true;

  /**
   * What information should be stored in the index, for scoring purposes. Defaults to docs but can
   * also be set to freqs to take term frequency into account when computing scores.
   */
  IndexOption index_options() default IndexOption.DOCS;

  /**
   * Whether field-length should be taken into account when scoring queries. Accepts true or false
   * (default).
   */
  boolean norms() default false;

  /**
   * Whether the field value should be stored and retrievable separately from the _source field.
   * Accepts true or false (default).
   */
  boolean store() default false;

  public static enum IndexOption {
    DOCS, FREQS;
  }
}
