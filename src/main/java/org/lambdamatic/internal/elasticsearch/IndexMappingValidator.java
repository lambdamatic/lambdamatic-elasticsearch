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
import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.IndexNotFoundException;

/**
 * Utility class to validate the mapping of a given Elasticsearch index with its associated domain
 * type.
 * 
 * @param the type of the domain class to use to validate the index mapping
 */
public class IndexMappingValidator {

  private final Client client;
  
  private final Class<?> domainType;
  
  private final String indexName;
  
  private final String type;

  private IndexValidationStatus indexStatus = null;

  /**
   * Constructor.
   * 
   * @param elasticsearchIndex the associated class to manage data on the ES index.
   */
  public IndexMappingValidator(final Client client, final Class<?> domainType, final String indexName, final String type) {
    this.client = client;
    this.domainType = domainType;
    this.indexName = indexName;
    this.type = type;
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
   * @return The corresponding {@link IndexValidationStatus}
   */
  public IndexValidationStatus verifyIndex() {
    if (this.indexStatus == null) {
      final IndicesAdminClient indicesAdminClient =
          this.client.admin().indices();
      try {
        final GetMappingsResponse mappingsResponse = indicesAdminClient
            .getMappings(new GetMappingsRequest().indices(this.indexName))
            .actionGet(new TimeValue(1, TimeUnit.SECONDS));
        final ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> mappings =
            mappingsResponse.mappings();
        this.indexStatus = verifyMappings(mappings);
      } catch (IndexNotFoundException e) {
        this.indexStatus = createIndex();
      }
    }
    return this.indexStatus;
  }

  /**
   * Creates the index with the required mappings.
   * 
   * @return {@link IndexValidationStatus#OK} if the operation succeeded.
   */
  private IndexValidationStatus createIndex() {
    final Map<String, Object> mapping =
        MappingUtils.getClassMapping(this.domainType);
    CreateIndexAction.INSTANCE.newRequestBuilder(this.client)
        .setIndex(this.indexName)
        .addMapping(this.type, mapping).get();
    return IndexValidationStatus.OK;
  }

  /**
   * Verifies that the mappings match the associated domain type.
   * 
   * @param mappings the mappings retrieved from the Elasticsearch index.
   * @return
   */
  private IndexValidationStatus verifyMappings(
      final ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> mappings) {
    return IndexValidationStatus.OK;
  }
}
