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

package org.lambdamatic.elasticsearch.querydsl;

import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Interface to iterate other elements of a {@link Stream}.
 * @param <T> the type of element
 */
public interface Iterable<T> {

  /**
   * Performs an action for each search result of this stream.
   * @param action the action to apply on each element
   *
   */
  void forEach(Consumer<? super T> action);
  
}
