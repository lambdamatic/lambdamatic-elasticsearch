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

import java.util.List;

/**
 * The result of a Search
 * @param <DomainType> the associated type of Document being searched
 * 
 */
public interface SearchResult<DomainType> {
  
  /**
   * @return the total number of matches on a queried index
   */
  public long getTotalCount();
  
  
  /**
   * @return the {@link List} of matching documents
   */
  public List<DomainType> getDocuments();

}
