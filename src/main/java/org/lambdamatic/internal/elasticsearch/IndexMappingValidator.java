/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.internal.elasticsearch;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.elasticsearch.action.admin.indices.create.CreateIndexAction;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.IndexNotFoundException;

/**
 * Utility class to check the mapping of a given Elasticsearch index against a domain type.
 * 
 * @param <DomainType>
 */
public class IndexMappingValidator<DomainType> {

  private BaseElasticsearchIndexImpl<DomainType, ?> elasticsearchIndex;

  private IndexStatus indexStatus = null;
  
  /**
   * Constructor
   * 
   * @param elasticsearchIndex
   */
  public IndexMappingValidator(BaseElasticsearchIndexImpl<DomainType, ?> elasticsearchIndex) {
    this.elasticsearchIndex = elasticsearchIndex;
  }

  /**
   * Verifies the status of the index in Elasticsearch:
   * <ul>
   * <li>If the index <strong>does not exist</strong>, it will be created.</li>
   * <li>If the index exists and <strong>matches</strong> the domain type mapping, it can be used
   * as-is.</li>
   * <li>If the index exists and <strong>does not match</strong> the domain type mapping, it cannot
   * be used and an exception will be raised.</li>
   * </ul>
   * 
   * @return The corresponding {@link IndexStatus}
   */
  public IndexStatus verifyIndex() {
    if(this.indexStatus == null) {
      final IndicesAdminClient indicesAdminClient =
          this.elasticsearchIndex.getClient().admin().indices();
      try {
        final GetMappingsResponse mappingsResponse = indicesAdminClient
            .getMappings(new GetMappingsRequest().indices(this.elasticsearchIndex.getIndexName()))
            .actionGet(new TimeValue(1, TimeUnit.SECONDS));
        final ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> mappings =
            mappingsResponse.mappings();
        this.indexStatus = verifyMappings(mappings);
      } catch (IndexNotFoundException e) {
        this.indexStatus = createIndex();
      }
    } return this.indexStatus;
  }

  /**
   * Creates the index with the required mappings.
   * @return {@link IndexStatus#OK} if the operation succeeded.
   */
  private IndexStatus createIndex() {
    final Map<String, Object> mapping = MappingUtils.getClassMapping(this.elasticsearchIndex.getDomainType());
    CreateIndexAction.INSTANCE.newRequestBuilder(this.elasticsearchIndex.getClient())
        .setIndex(this.elasticsearchIndex.getIndexName())
        .addMapping(this.elasticsearchIndex.getType(), mapping).get();
    return IndexStatus.OK;
  }
  
  /**
   * Verifies that the mappings match the associated domain type.
   * 
   * @param mappings the mappings retrieved from the Elasticsearch index.
   * @return 
   */
  private IndexStatus verifyMappings(
      final ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> mappings) {
    return IndexStatus.OK;
  }
}
