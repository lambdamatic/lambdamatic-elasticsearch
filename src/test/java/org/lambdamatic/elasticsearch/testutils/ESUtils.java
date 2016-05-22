package org.lambdamatic.elasticsearch.testutils;

import org.elasticsearch.action.admin.cluster.stats.ClusterStatsRequest;
import org.elasticsearch.action.admin.cluster.stats.ClusterStatsResponse;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsRequest;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsResponse;
import org.elasticsearch.client.Client;

public class ESUtils {

  /**
   * Counts the number of documents in the whole cluster.
   * 
   * @param client the Elascticsearch client
   * @return the total number of documents in the cluster
   */
  public static long countDocs(final Client client) {
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
  public static long countDocs(final Client client, final String indexName) {
    final IndicesStatsResponse indexStats =
        client.admin().indices().stats(new IndicesStatsRequest().indices(indexName)).actionGet();
    return indexStats.getIndex(indexName).getTotal().docs.getCount();
  }
}
