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
 */
public interface FullTextField {

  /**
   * @param word the word to search.
   */
  @QueryClauseType(EnumQueryClauseType.MATCHES)
  public boolean matches(String word);

  @QueryClauseType(EnumQueryClauseType.MATCHES)
  public Boostable matching(String word);
}
