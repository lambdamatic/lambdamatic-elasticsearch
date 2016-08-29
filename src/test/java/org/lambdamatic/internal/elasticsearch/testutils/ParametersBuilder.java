/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.internal.elasticsearch.testutils;

import java.util.ArrayList;
import java.util.List;

import org.junit.runners.Parameterized;
import org.lambdamatic.elasticsearch.searchdsl.QueryExpression;
import org.lambdamatic.internal.elasticsearch.searchdsl.Query;

/**
 * A utility class to provide the dataset for JUnit tests to be run with {@link Parameterized}.
 */
public class ParametersBuilder<T> {

  /** The data. */
  private final List<Object[]> dataset = new ArrayList<>();

  /**
   * Adds the given array of objects in the dataset.
   * 
   * @param expression the {@link QueryExpression} to evaluate
   * @param expectedQuery the expected {@link Query}
   * @return the current instance of {@link ParametersBuilder}
   */
  public ParametersBuilder<T> add(final QueryExpression<T> expression, final Query expectedQuery) {
    this.dataset.add(new Object[] {expression, expectedQuery});
    return this;
  }

  /**
   * Builds the dataset.
   * 
   * @return the dataset as an array of objects.
   */
  public List<Object[]> get() {
    return this.dataset;
  }

}
