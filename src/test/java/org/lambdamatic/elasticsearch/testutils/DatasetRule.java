/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.elasticsearch.testutils;

import static org.assertj.core.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

import org.assertj.core.api.Assertions;
import org.elasticsearch.action.admin.cluster.stats.ClusterStatsRequest;
import org.elasticsearch.action.admin.cluster.stats.ClusterStatsResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.shard.DocsStats;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JUnit Rule to load a dataset specified by the {@link Dataset} annotation before a test is
 * executed.
 */
public class DatasetRule implements MethodRule {

  /** The logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(DatasetRule.class);

  /**
   * The {@link Client} to connect to the ES node(s).
   */
  private final Client client;

  /**
   * Constructor
   * 
   * @param client the {@link Client} to connect to the ES node(s).
   */
  public DatasetRule(Client client) {
    this.client = client;
  }


  @Override
  public Statement apply(Statement base, FrameworkMethod method, Object target) {
    try {
      cleanIndices();
      createIndices(method.getMethod().getAnnotation(Dataset.class));
      loadDocuments(method.getMethod().getAnnotation(Dataset.class));
    } catch (IOException e) {
      fail("Failed to load the specified dataset", e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      fail("Failed to load the specified dataset", e);
    }
    return base;
  }

  /**
   * Deletes all indices and their data.
   */
  private void cleanIndices() {
    final IndicesAdminClient indicesAdminClient = client.admin().indices();
    final GetIndexResponse getIndexResponse =
        indicesAdminClient.prepareGetIndex().execute().actionGet();
    Stream.of(getIndexResponse.getIndices()).forEach(index -> {
      LOGGER.debug("Deleting index {}", index);
      indicesAdminClient.delete(new DeleteIndexRequest(index));
    });
  }

  /**
   * Create the indices from the resource specified in the given {@link Dataset#settings()} attribute.
   * 
   * @param dataset the {@link Dataset} specification
   * @throws IOException if an I/O error occurs while closing the {@link InputStream} for the
   *         dataset location.
   * @throws InterruptedException may occur while thread is sleeping, to give time to Elasticsearch
   *         to actually index the data that was sent.
   */
  private void createIndices(final Dataset dataset) throws IOException, InterruptedException {
    if (dataset == null || dataset.settings() == null || dataset.settings().isEmpty()) {
      LOGGER.warn("Skipping dataset loading");
      return;
    }
    LOGGER.info("Configuring indices using '{}'", dataset.documents());
    try (
        final InputStream jsonStream =
            Thread.currentThread().getContextClassLoader().getResourceAsStream(dataset.settings());
        final JsonReader jsonReader = Json.createReader(jsonStream);) {
      final JsonObject root = jsonReader.readObject();
      if (root == null) {
        LOGGER.info("No index settings found in the '{}' file.", dataset.settings());
      } else {
        root.entrySet().stream().forEach(indexEntry -> {
          final String indexName = indexEntry.getKey();
          // prepare the index creation
          final CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);
          // update the index with the specific configuration
          final JsonObject indexSettings = (JsonObject) indexEntry.getValue();
          createIndexRequest.settings(indexSettings.toString());
          // locate and parse the optional 'mappings' element
          final JsonObject mappings = indexSettings.getJsonObject("mappings");
          if (mappings == null) {
            LOGGER.info("No 'mappings' entry found for index {} in dataset file.", indexName);
          } else {
            // include the type mappings in the index creation request
            mappings.entrySet().forEach(typeMapping -> {
              createIndexRequest.mapping(typeMapping.getKey(), typeMapping.getValue().toString());
            });
          }
          // create the index with all the config
          client.admin().indices().create(createIndexRequest).actionGet();
          // wait for shards to be ready
        });
      }
    } finally {
      LOGGER.info("Indices configuration done.");

    }
  }
  
  /**
   * Loads the documents from the resource specified in the given {@link Dataset#documents()} attribute.
   * 
   * @param dataset the {@link Dataset} specification
   * @throws IOException if an I/O error occurs while closing the {@link InputStream} for the
   *         dataset location.
   * @throws InterruptedException may occur while thread is sleeping, to give time to Elasticsearch
   *         to actually index the data that was sent.
   */
  private void loadDocuments(final Dataset dataset) throws IOException, InterruptedException {
    if (dataset == null || dataset.documents() == null || dataset.documents().isEmpty()) {
      LOGGER.warn("Skipping dataset loading");
      return;
    }
    LOGGER.info("Indexing documents using '{}'", dataset.documents());
    try (
        final InputStream jsonStream =
        Thread.currentThread().getContextClassLoader().getResourceAsStream(dataset.documents());
        final JsonReader jsonReader = Json.createReader(jsonStream);) {
      final JsonObject root = jsonReader.readObject();
      final JsonArray documents = root.getJsonArray("documents");
      if (documents == null) {
        LOGGER.info("No 'documents' array defined in the dataset.");
        return;
      }
      documents.stream()
          // convert each entry into a JsonObject
          .map(doc -> (JsonObject) doc)
          // for each 'document'
          .forEach(doc -> {
            // the index in which the document should be stored
            final JsonArray documentIndices = getIndices(doc);
            // the document to store
            final JsonObject data = getDocumentData(doc);
            // add the document into the indices
            documentIndices.stream().map(index -> (JsonObject) index).forEach(index -> {
              final String indexName = index.getString("indexName");
              final String indexType = index.getString("indexType");
              final String indexId = index.getString("indexId");
              LOGGER.debug("Adding document at {}/{}/{}: {}", indexName, indexType, indexId, data);
              final IndexResponse indexResponse = client.prepareIndex(indexName, indexType)
                  .setId(indexId).setSource(data).execute().actionGet();
              Assertions.assertThat(indexResponse.isCreated()).isEqualTo(true);
            });
          });
      // give Elasticsearch a bit of time to actually index all the data that was sent
      if (!documents.isEmpty()) {
        long docCount = 0L;
        while ((docCount = countDocs()) < documents.size()) {
          Thread.sleep(TimeUnit.SECONDS.toMillis(1));
        }
      }
    } finally {
      LOGGER.info("Loading dataset done.");
      
    }
  }

  /**
   * @return the <strong>current</strong> number of documents in the indices.
   */
  private long countDocs() {
    final ClusterStatsResponse clusterStats =
        client.admin().cluster().clusterStats(new ClusterStatsRequest()).actionGet();
    return clusterStats.getIndicesStats().getDocs().getCount();
  }

  /**
   * Retrieves the value of the <code>indices</code> entry from the given <code>doc</code>
   * {@link JsonObject}
   * 
   * @param doc the {@link JsonObject} to analyze
   * @return the <code>indices</code> entry value as a {@link JsonArray}
   * @throws InvalidDatasetException if no entry named <code>index</code> could be found.
   */
  private JsonArray getIndices(final JsonObject doc) {
    final Entry<String, JsonValue> indicesEntry =
        doc.entrySet().stream().filter(entry -> entry.getKey().equals("indices")).findFirst()
            .orElseThrow(() -> new InvalidDatasetException(
                "Dataset did not contain an 'indices' entry in the document JSON object."));
    final JsonArray indices = (JsonArray) indicesEntry.getValue();
    return indices;
  }

  /**
   * Retrieves the value of the <code>data</code> entry from the given <code>doc</code>
   * {@link JsonObject}
   * 
   * @param doc the {@link JsonObject} to analyze
   * @return the <code>data</code> entry value as a {@link JsonObject}
   * @throws InvalidDatasetException if no entry named <code>data</code> could be found.
   */
  private JsonObject getDocumentData(final JsonObject doc) {
    final Entry<String, JsonValue> dataEntry =
        doc.entrySet().stream().filter(entry -> entry.getKey().equals("data")).findFirst()
            .orElseThrow(() -> new InvalidDatasetException(
                "Dataset did not contain an 'data' entry in the document JSON object."));
    final JsonObject data = (JsonObject) dataEntry.getValue();
    return data;
  }

}
