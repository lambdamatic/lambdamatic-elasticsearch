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
import java.util.Map.Entry;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

import org.assertj.core.api.Assertions;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexResponse;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesAdminClient;
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

  private final static Logger LOGGER = LoggerFactory.getLogger(DatasetRule.class);

  /** The {@link Client} to connect to the ES node(s). */
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
      loadDataset(method.getMethod().getAnnotation(Dataset.class));
    } catch (IOException e) {
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
   * Loads data from the resource specified in the given {@link Dataset} argument.
   * 
   * @param dataset the {@link Dataset} specification
   * @throws IOException if an I/O error occurs while closing the {@link InputStream} for the
   *         dataset location.
   */
  private void loadDataset(final Dataset dataset) throws IOException {
    if (dataset == null || dataset.location() == null || dataset.location().isEmpty()) {
      return;
    }
    try (
        final InputStream jsonStream =
            Thread.currentThread().getContextClassLoader().getResourceAsStream(dataset.location());
        final JsonReader jsonReader = Json.createReader(jsonStream);) {
      final JsonObject root = jsonReader.readObject();
      // locate the 'documents' entry
      final Entry<String, JsonValue> documentsEntry =
          root.entrySet().stream().filter(entry -> entry.getKey().equals("documents")).findFirst()
              .orElseThrow(() -> new InvalidDatasetException(
                  "Dataset did not contain a 'documents' entry in the root JSON object."));
      final JsonArray documents = (JsonArray) documentsEntry.getValue();
      documents.stream()
          // convert each entry into a JsonObject
          .map(doc -> (JsonObject) doc)
          // for each 'document'
          .forEach(doc -> {
            // TODO: support multiple indices per document
            // TODO: support index mapping
            // the index in which the document should be stored
            final JsonObject index = getIndex(doc);
            final String indexName = index.getString("indexName");
            final String indexType = index.getString("indexType");
            final String indexId = index.getString("indexId");
            client.admin().indices().prepareCreate(indexName).execute();
            // the document to store
            final JsonObject data = getDocumentData(doc);
            // add the document into the index
            final IndexResponse indexResponse = client.prepareIndex(indexName, indexType)
                .setId(indexId).setSource(data).execute().actionGet();
            Assertions.assertThat(indexResponse.isCreated()).isEqualTo(true);
          });
    } ;
  }

  /**
   * Retrieves the value of the <code>index</code> entry from the given <code>doc</code>
   * {@link JsonObject}
   * 
   * @param doc the {@link JsonObject} to analyze
   * @return the <code>index</code> entry value as a {@link JsonObject}
   * @throws InvalidDatasetException if no entry named <code>index</code> could be found.
   */
  private JsonObject getIndex(final JsonObject doc) {
    final Entry<String, JsonValue> indexEntry =
        doc.entrySet().stream().filter(entry -> entry.getKey().equals("index")).findFirst()
            .orElseThrow(() -> new InvalidDatasetException(
                "Dataset did not contain an 'index' entry in the document JSON object."));
    final JsonObject index = (JsonObject) indexEntry.getValue();
    return index;
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
