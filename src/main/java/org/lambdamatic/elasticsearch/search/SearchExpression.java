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

import org.lambdamatic.SerializablePredicate;

/**
 * A {@link SearchExpression} which specifies which {@code DomainType} should be part of the
 * search operation.
 * @param <DomainType>
 * 
 */
@FunctionalInterface
public interface SearchExpression<DomainType> extends SerializablePredicate<DomainType> {
  //this predicate interface has no extra method.
}
