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

package org.lambdamatic.elasticsearch.testutils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.runners.Parameterized;

/**
 * A utility class to provide the dataset for JUnit tests to be run with {@link Parameterized}. 
 */
public class ParametersBuilder<T> {

  /** The data. */
  private final List<Object[]> dataset = new ArrayList<>();
  
  /**
   * Adds the given array of objects in the dataset.
   * @param firstItem the first item
   * @param otherItems the other items
   * @return the current instance of {@link ParametersBuilder} 
   */
  public ParametersBuilder<T> add(final T firstItem, Object...otherItems) {
    final Object[] allItems = merge(firstItem, otherItems);
    this.dataset.add(allItems);
    return this;
  }
  
  /**
   * Merges the first item with all others in a single, new array.
   * @param firstItem the first item
   * @param otherItems the other items
   * @return the single, resulting array
   */
  private static <T> Object[] merge(final T firstItem, Object... otherItems) {
    final Object[] allItems = new Object[otherItems.length + 1];
    allItems[0] = firstItem;
    System.arraycopy(otherItems, 0, allItems, 1, otherItems.length);
    return allItems;
  }

  /**
   * Builds the dataset
   * @return the dataset as an array of objects.
   */
  public List<Object[]> get() {
    return this.dataset;
  }
  
}
