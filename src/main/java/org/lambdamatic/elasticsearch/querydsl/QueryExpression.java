/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.elasticsearch.querydsl;

import org.lambdamatic.SerializableConsumer;

/**
 * A {@link QueryExpression} which specifies which {@literal domain type} should be part of the
 * query operation.
 * 
 * @param <M> the metadata associated with the type of documents to search. The metadata provides
 *        the user with a custom DSL, that will depend on the type of operation that is expressed
 *        (ie, filtering vs querying).
 * 
 */
@FunctionalInterface
public interface QueryExpression<M> extends SerializableConsumer<M> {
  // this predicate interface has no extra method.
}
