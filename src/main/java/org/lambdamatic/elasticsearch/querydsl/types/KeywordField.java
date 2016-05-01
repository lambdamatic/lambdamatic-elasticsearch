/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.elasticsearch.querydsl.types;

import org.lambdamatic.internal.elasticsearch.SearchOperation;
import org.lambdamatic.internal.elasticsearch.SearchOperation.EnumSearchType;

/**
 * Interface designating elements that can be searched and exposing an API for all kind of searches
 * that can be performed on the field (fuzzy, range, etc.)
 */
public interface KeywordField {

  /**
   * @param word the word to search
   * @return a {@link Boostable} to optionally boost the result on the query clause.
   */
  @SearchOperation(EnumSearchType.TERM)
  public Boostable hasExactTerm(String word);

  
}
