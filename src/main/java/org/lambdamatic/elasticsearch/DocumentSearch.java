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

package org.lambdamatic.elasticsearch;

import org.lambdamatic.elasticsearch.querydsl.FilterContext;
import org.lambdamatic.elasticsearch.querydsl.MustMatchContext;
import org.lambdamatic.elasticsearch.querydsl.ShouldMatchContext;
import org.lambdamatic.internal.elasticsearch.QueryMetadata;

/**
 * Interface for document search.
 * 
 * @param <D> the type of documents stored in the associated index.
 * @param <Q> the {@link QueryMetadata} type associated with the type of documents.
 */
public interface DocumentSearch<D, Q> extends ShouldMatchContext<D, Q>, MustMatchContext<D, Q>, FilterContext<D, Q> {

  
}
