/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.elasticsearch.search;

import org.lambdamatic.internal.elasticsearch.SearchOperation;
import org.lambdamatic.internal.elasticsearch.SearchOperation.EnumSearchType;

/**
 * Interface designating elements that can be searched and exposing an API for all kind of searches
 * that can be performed on the field (fuzzy, range, etc.)
 */
public interface SearchableElement {

  /**
   * @param word the word to search
   * @return a {@link MatchingElement} to allow for fine-grained configuration on the matching
   *         element.
   */
  @SearchOperation(EnumSearchType.MATCHES)
  public MatchingElement matches(String word);

  /**
   * <strong>fuzzy search</strong> on a term similar for the given <code>word</code>.
   * 
   * @param word the work to match
   * @return a boolean operand, in order to include this operation in a more complex expression.
   */
  @SearchOperation(EnumSearchType.FUZZY)
  public boolean similar(String word);
}
