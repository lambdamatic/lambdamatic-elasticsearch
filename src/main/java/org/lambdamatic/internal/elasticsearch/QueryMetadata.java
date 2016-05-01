/*******************************************************************************
 * Copyright (c) 2014, 2015 Red Hat, Inc. Distributed under license by Red Hat, Inc. All rights
 * reserved. This program is made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/

package org.lambdamatic.internal.elasticsearch;

/**
 * Marker interface for generated metadata classes used to express query clauses.
 * 
 * @param <D> the actual domain type being queried.
 *
 */
//FIXME: do we need to split the QueryMetadata into FilterContextMetadata vs QueryContextMetadata and thus
// generate different classes for a domain type, in order to restrict the comparisons that can be expressed ?
// EG: can we _use_ the 'match' in a FILTER context ? 

public interface QueryMetadata<D> {
  // empty marker interface
}
