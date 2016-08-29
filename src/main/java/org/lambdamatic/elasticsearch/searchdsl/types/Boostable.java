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

package org.lambdamatic.elasticsearch.searchdsl.types;

import org.lambdamatic.elasticsearch.searchdsl.types.QueryOperator.QueryOperatorType;

/**
 * Operand to boost the result.
 * @param <T> the type of field that is boostable
 */
public interface Boostable {

  /**
   * Boosts the result on the given field.
   * @param factor the boost factor
   * @return the current field, for method call chaining
   */
  @QueryOperator(QueryOperatorType.BOOST)
  public boolean boost(float factor);
}
