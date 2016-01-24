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
 * Defines the Elasticsearch field in a document.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DocumentField {

  /**
   * Name of the field in the stored document. If unspecified, the {@link DocumentField} name is
   * used.
   */
  public String name() default "";

  /**
   * Flag to indicate if the annotated {@link DocumentField} is the document id.
   */
  public boolean id() default false;

  /**
   * Flag to indicate if the annotated {@link DocumentField} is stored in the index.
   */
  public boolean stored() default false;

  /**
   * Flag to indicate if this field should be part of the <quote>_all</quote> field of this parent
   * document.
   */
  public boolean includeInAllField() default true;

  /**
   * The copy_to parameter allows you to create custom <code>_all</code> fields. In other words, the
   * values of multiple fields can be copied into a group field, which can then be queried as a
   * single field.
   * 
   * See
   * <a href="https://www.elastic.co/guide/en/elasticsearch/reference/2.1/copy-to.html">copy_to</a>
   * in Elasticsearch docs.
   */
  public String copyTo() default "";
}
