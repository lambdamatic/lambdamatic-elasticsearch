/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.elasticsearch.searchdsl.types;

import org.lambdamatic.elasticsearch.searchdsl.types.QueryClauseType.EnumQueryClauseType;

/**
 * Interface designating elements that can be searched and exposing an API for all kind of searches
 * that can be performed on the field (fuzzy, range, etc.)
 * @param <T> the type of the field to query. Can be a {@link String} or an {@link Enum}.
 */
public interface KeywordField<T> {

  /**
   * @param word the word to search
   * @return a boolean to terminate this query fragment.
   */
  @QueryClauseType(EnumQueryClauseType.TERM)
  public boolean hasExactTerm(T word);

  /**
   * @param word the word to search
   * @return a {@link Boostable} to optionally boost the result on the query clause.
   */
  @QueryClauseType(EnumQueryClauseType.TERM)
  public Boostable havingExactTerm(T word);


  /**
   * @param value the value to compare to
   * @return a boolean to terminate this query fragment.
   */
  @QueryClauseType(EnumQueryClauseType.RANGE_GT)
  public boolean isGreaterThan(T value);

  /**
   * @param value the value to compare to
   * @return a {@link Boostable} to optionally boost the result on the query clause.
   */
  @QueryClauseType(EnumQueryClauseType.RANGE_GT)
  public Boostable beingGreaterThan(T value);

  /**
   * @param value the value to compare to
   * @return a boolean to terminate this query fragment.
   */
  @QueryClauseType(EnumQueryClauseType.RANGE_GTE)
  public boolean isGreaterOrEqualTo(T value);
  
  /**
   * @param value the value to compare to
   * @return a {@link Boostable} to optionally boost the result on the query clause.
   */
  @QueryClauseType(EnumQueryClauseType.RANGE_GTE)
  public Boostable beingGreaterOrEqualTo(T value);
  


}
