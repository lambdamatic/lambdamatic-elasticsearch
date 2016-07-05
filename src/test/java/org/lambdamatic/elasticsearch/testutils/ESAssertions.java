/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.elasticsearch.testutils;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.assertj.core.api.AbstractAssert;
import org.elasticsearch.action.admin.cluster.stats.ClusterStatsRequest;
import org.elasticsearch.action.admin.cluster.stats.ClusterStatsResponse;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsRequest;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsResponse;
import org.elasticsearch.client.Client;

/**
 * Assertions on Elasticsearch.
 */
public class ESAssertions extends AbstractAssert<ESAssertions, Client> {

  private static final long timeout = TimeUnit.SECONDS.toMillis(5);

  /**
   * Public accessor.
   * 
   * @param client the {@link Client}
   * @return the {@link ESAssertions}
   */
  public static ESAssertions assertThat(final Client client) {
    return new ESAssertions(client);
  }

  /**
   * Constructor.
   * 
   * @param client the {@link Client}
   * @param indexName the name of the index to query
   */
  protected ESAssertions(final Client client) {
    super(client, ESAssertions.class);
  }

  /**
   * Verifies the number of documents in the whole cluster, with retries and a default timeout, as
   * Elasticsearch may need a bit of time to index all incoming docs.
   * 
   * @param expectedCount the expected number of documents
   * @return this {@link ESAssertions} for method calls chaining.
   */
  public ESAssertions hasClusterSize(final long expectedCount) {
    final long actualCount = doCountWithTimeout(expectedCount, () -> countDocs(actual), timeout);
    if (actualCount != expectedCount) {
      failWithMessage("Expected <%s> documents in the cluster but there were only <%s>",
          expectedCount, actualCount);
    }
    return this;
  }

  /**
   * Verifies the number of documents in the given index, with retries and a default timeout, as
   * Elasticsearch may need a bit of time to index all incoming docs.
   * 
   * @param indexName the name of the index in which to count documents
   * @param expectedCount the expected number of documents
   * @return this {@link ESAssertions} for method calls chaining.
   */
  public ESAssertions hasIndexSize(final String indexName, final long expectedCount) {
    final long actualCount = doCountWithTimeout(expectedCount, () -> countDocs(actual, indexName), timeout);
    if (actualCount != expectedCount) {
      failWithMessage("Expected <%s> documents in index <%s> but there were only <%s>",
          expectedCount, indexName, actualCount);
    }
    return this;
  }

  private long doCountWithTimeout(final long expectedCount, final Supplier<Long> countSupplier, final long timeout) {
    final long startTime = System.currentTimeMillis();
    while (System.currentTimeMillis() < startTime + timeout) {
      try {
        Thread.sleep(TimeUnit.MILLISECONDS.toMillis(100));
      } catch (InterruptedException e) {
        Thread.interrupted();
      }
      final long actualCount = countSupplier.get();
      if (actualCount == expectedCount) {
        return actualCount;
      }
    }
    // recount a last time, giving an ultimate chance...
    return countSupplier.get();
  }

  /**
   * Counts the number of documents in the whole cluster.
   * 
   * @param client the Elascticsearch client
   * @return the total number of documents in the cluster
   */
  private static long countDocs(final Client client) {
    final ClusterStatsResponse clusterStats =
        client.admin().cluster().clusterStats(new ClusterStatsRequest()).actionGet();
    return clusterStats.getIndicesStats().getDocs().getCount();
  }

  /**
   * Counts the number of documents in the given index.
   * 
   * @param client the Elascticsearch client
   * @param indexName the name of the index
   * @return the total number of documents in the index (in all shards)
   */
  private static long countDocs(final Client client, final String indexName) {
    final IndicesStatsResponse indexStats =
        client.admin().indices().stats(new IndicesStatsRequest().indices(indexName)).actionGet();
    return indexStats.getIndex(indexName).getTotal().docs.getCount();
  }
}
