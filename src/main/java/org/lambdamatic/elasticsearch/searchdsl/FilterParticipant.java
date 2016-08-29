/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.elasticsearch.searchdsl;

import org.lambdamatic.internal.elasticsearch.QueryMetadata;

/**
 * Interface to define the {@literal FILTER} part of a document query.
 * 
 * @param <D> the type of documents stored in the associated index.
 * @param <Q> the {@link QueryMetadata} type associated with the type of documents.
 */
public interface FilterParticipant<D, Q> {

  /**
   * The clause (query) <strong>must</strong> appear in matching documents. However <strong>the
   * score of the query will be ignored</strong>.
   * 
   * @param expression the <strong>filter expression</strong> to pass in the document search request
   */
  public CollectableContext<D, Q> filter(QueryExpression<Q> expression);


}
