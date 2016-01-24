/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.internal.elasticsearch;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.lambdamatic.elasticsearch.search.SearchableElement;

/**
 * Designate the type of search that a method on a {@link SearchableElement} provides.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SearchOperation {
  
  /**
   * The type of search.
   */
  public enum EnumSearchType {
    /** Fuzzy search. */
    FUZZY, 
    /** Proximity search. */
    PROXIMITY, 
    /** Range search. */
    RANGE, 
    /** Match search. */
    MATCHES;
  }

  /** the type of search. */
  public EnumSearchType value();
}
