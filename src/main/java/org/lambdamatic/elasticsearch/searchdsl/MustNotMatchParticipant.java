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
 * Interface to define the {@literal query} part of a document request.
 * 
 * @param <D> the type of the domain document.
 * @param <Q> the {@link QueryMetadata} type associated with the type of documents.
 */
public interface MustNotMatchParticipant<D, Q> {

  /**
   * The clause (query) <strong>must not</strong> appear in matching documents and will contribute
   * to the score.
   * 
   * @param queryExpression the <strong>query expression</strong> to pass in the document search
   *        request
   */
  public MustNotMatchContext<D, Q> mustNotMatch(QueryExpression<Q> queryExpression);

}
