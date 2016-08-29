/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.elasticsearch;

import org.lambdamatic.elasticsearch.searchdsl.FilterParticipant;
import org.lambdamatic.elasticsearch.searchdsl.MustMatchParticipant;
import org.lambdamatic.elasticsearch.searchdsl.MustNotMatchParticipant;
import org.lambdamatic.elasticsearch.searchdsl.ShouldMatchParticipant;
import org.lambdamatic.internal.elasticsearch.QueryMetadata;

/**
 * Interface for document querying.
 * 
 * @param <D> the type of documents stored in the associated index.
 * @param <Q> the {@link QueryMetadata} type associated with the type of documents.
 */
public interface DocumentQuery<D, Q>
    // TODO: add more contexts when support for sorting, size/limit, etc. are supported.
    extends ShouldMatchParticipant<D, Q>, MustMatchParticipant<D, Q>, MustNotMatchParticipant<D, Q>,
    FilterParticipant<D, Q> {



}
