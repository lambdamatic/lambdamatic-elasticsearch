/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.elasticsearch.annotations.analyzers;

import java.util.Collections;
import java.util.List;

/**
 * Field annotated with {@link StandardAnalyzer} are indexed with the built-in
 * <code>Standard Analyzer</code>.
 * 
 * See <a href=
 * "https://www.elastic.co/guide/en/elasticsearch/reference/2.1/analysis-standard-analyzer.html">
 * Standard Analyzer</a> in the Elasticsearch documentation.
 */
@Analyzer("standard")
public interface StandardAnalyzer { //TODO: extends Analyzer ?

  /**
   * @return A list of stopwords to initialize the stop filter with. Defaults to an empty stopword
   *         list Check Stop Analyzer for more details.
   * 
   */
  public default List<String> stopWords() {
    return Collections.emptyList();
  }

  /**
   * @return The maximum token length. If a token is seen that exceeds this length then it is split
   *         at max_token_length intervals. Defaults to 255.
   */

  public default int maxTokenLength() {
    return 255;
  }
}
