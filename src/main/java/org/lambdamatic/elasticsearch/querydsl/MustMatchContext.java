/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.elasticsearch.querydsl;

import org.lambdamatic.internal.elasticsearch.QueryMetadata;
import org.reactivestreams.Publisher;

/**
 * Interface to define the {@literal SHOULD} part of a search query.
 * 
 * @param <D> the type of documents stored in the associated index.
 * @param <Q> the {@link QueryMetadata} type associated with the type of documents.
 */
public interface MustMatchContext<D, Q> extends FilterContext<D, Q> {

  /**
   * Matches documents in the index associated with the domain type.
   * 
   * @param expression the <strong>MUST MATCH</strong> expression to pass in the query.
   * @return A <a href=
   *         "https://github.com/reactive-streams/reactive-streams-jvm/blob/v1.0.0/README.md">
   *         Reactive Streams</a> {@link Publisher} for the domain-type specific search result.
   */
  public FilterContext<D, Q> mustMatch(QueryExpression<Q> expression);
}
