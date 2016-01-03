/*******************************************************************************
 * Copyright (c) 2016 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.elasticsearch.search;

/**
 * Interface designating elements that can be searched.  
 */
public interface SearchableElement {
  
  /**
   * @param term the term to search
   * @return a {@link MatchingElement} to allow for fine-grained configuration on the matching element.
   */
  public MatchingElement matches(String term);

}
