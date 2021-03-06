/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.elasticsearch.searchdsl;

import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.lambdamatic.internal.elasticsearch.QueryMetadata;

/**
 * @param <D> the domain type of the search operation.
 * @param <Q> the {@link QueryMetadata} type associated with the type of documents.
 */
public interface CollectableContext<D, Q> {

  /**
   * Performs a <a href="package-summary.html#MutableReduction">mutable reduction</a> operation on
   * the elements of this stream using a {@code Collector}. A {@code Collector} encapsulates the
   * functions used as arguments to {@link #collect(Supplier, BiConsumer, BiConsumer)}, allowing for
   * reuse of collection strategies and composition of collect operations such as multiple-level
   * grouping or partitioning.
   *
   * <p>
   * If the stream is parallel, and the {@code Collector} is
   * {@link Collector.Characteristics#CONCURRENT concurrent}, and either the stream is unordered or
   * the collector is {@link Collector.Characteristics#UNORDERED unordered}, then a concurrent
   * reduction will be performed (see {@link Collector} for details on concurrent reduction.)
   *
   * <p>
   * This is a <a href="package-summary.html#StreamOps">terminal operation</a>.
   *
   * <p>
   * When executed in parallel, multiple intermediate results may be instantiated, populated, and
   * merged so as to maintain isolation of mutable data structures. Therefore, even when executed in
   * parallel with non-thread-safe data structures (such as {@code ArrayList}), no additional
   * synchronization is needed for a parallel reduction.
   *
   * @apiNote The following will accumulate strings into an ArrayList:
   * 
   *          <pre>
   * {@code
   *     List<String> asList = stringStream.collect(Collectors.toList());
   * }
   *          </pre>
   *
   *          <p>
   *          The following will classify {@code Person} objects by city:
   * 
   *          <pre>
   * {@code
   *     Map<String, List<Person>> peopleByCity
   *         = personStream.collect(Collectors.groupingBy(Person::getCity));
   * }
   *          </pre>
   *
   *          <p>
   *          The following will classify {@code Person} objects by state and city, cascading two
   *          {@code Collector}s together:
   * 
   *          <pre>
   * {@code
   *     Map<String, Map<String, List<Person>>> peopleByStateAndCity
   *         = personStream.collect(Collectors.groupingBy(Person::getState,
   *                                                      Collectors.groupingBy(Person::getCity)));
   * }
   *          </pre>
   *
   * @param <R> the type of the result
   * @param <A> the intermediate accumulation type of the {@code Collector}
   * @param collector the {@code Collector} describing the reduction
   * @return the result of the reduction
   * @see #collect(Supplier, BiConsumer, BiConsumer)
   * @see Collectors
   */
  <R, A> R collect(Collector<? super D, A, R> collector);
}
