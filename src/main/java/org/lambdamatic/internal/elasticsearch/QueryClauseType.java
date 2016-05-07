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

import org.lambdamatic.elasticsearch.querydsl.types.FullTextField;

/**
 * Designate the type of search that a method on a {@link FullTextField} provides.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface QueryClauseType {
  
  /**
   * The type of query clause.
   */
  public enum EnumQueryClauseType {
    /** Fuzzy search. */
    FUZZY, 
    /** Proximity search. */
    PROXIMITY, 
    /** Range search. */
    RANGE, 
    /** Match search. */
    MATCHES, 
    /** Term search. */
    TERM;
  }

  /** the type of search. */
  public EnumQueryClauseType value();
}
