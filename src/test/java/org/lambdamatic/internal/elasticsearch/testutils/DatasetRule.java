/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.internal.elasticsearch.testutils;

import static org.assertj.core.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;

import org.assertj.core.api.Assertions;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.lambdamatic.elasticsearch.exceptions.ConversionException;
import org.lambdamatic.internal.elasticsearch.clientdsl.Client;
import org.lambdamatic.internal.elasticsearch.clientdsl.responses.IndexDocumentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JUnit Rule to clear and index and load a dataset specified by the {@link Dataset} annotation
 * before a test is executed.
 */
public class DatasetRule implements TestRule {

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
  public DatasetRule(final Client client) {
    this.client = client;
  }


  @Override
  public Statement apply(final Statement base, final Description description) {
    try {
      final List<JsonObject> confis = getIndexConfigurations(description);
      final List<JsonObject> documents = getDocuments(description);
      deleteIndices(confis);
      createIndices(confis);
      indexDocuments(documents);
    } catch (IOException e) {
      fail("Failed to load the specified dataset", e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      fail("Failed to load the specified dataset", e);
    }
    return base;
  }

  private List<JsonObject> getIndexConfigurations(final Description description) {
    final Dataset[] datasetAnnotations = lookupAnnotation(description, Dataset.class);
    if (datasetAnnotations == null || datasetAnnotations.length == 0) {
      LOGGER.info("Skipping index configuration: no @Dataset annotation available.");
      return Collections.emptyList();
    }
    return Stream.of(datasetAnnotations).map(annotation -> (Dataset) annotation)
        .filter(datasetAnnotation -> datasetAnnotation.settings() != null
            && !datasetAnnotation.settings().isEmpty())
        .map(datasetAnnotation -> {
          LOGGER.info("Configuring indice settings using '{}'", datasetAnnotation.settings());
          try (
              final InputStream jsonStream = Thread.currentThread().getContextClassLoader()
                  .getResourceAsStream(datasetAnnotation.settings());
              final JsonReader jsonReader = Json.createReader(jsonStream);) {
            return jsonReader.readObject();
          } catch (IOException e) {
            e.printStackTrace();
            fail("Failed to read file " + datasetAnnotation.settings() + ": " + e.getMessage());
          }
          return null;
        }).collect(Collectors.toList());
  }


  /**
   * Looks-up the given {@code annotation} on the current test method or on the test class.
   * 
   * @param description the test {@link Description}
   * @param annotationClass the annotation class to find
   * @return the matching annotation or <code>null</code> if it was not found
   */
  private <T extends Annotation> T[] lookupAnnotation(final Description description,
      final Class<T> annotationClass) {
    try {
      final Class<?> testClass = description.getTestClass();
      if (description.getMethodName() != null) {
        final Method testMethod = testClass.getMethod(description.getMethodName());
        if (testMethod.getAnnotationsByType(annotationClass) != null) {
          return testMethod.getAnnotationsByType(annotationClass);
        }
      }
      return testClass.getAnnotationsByType(annotationClass);
    } catch (NoSuchMethodException | SecurityException e) {
      e.printStackTrace();
      fail("Failed to look-up annotation " + annotationClass.toString() + ": " + e.getMessage());
      return null;
    }
  }

  private List<JsonObject> getDocuments(final Description description) {
    final Dataset[] datasetAnnotations = lookupAnnotation(description, Dataset.class);
    if (datasetAnnotations == null || datasetAnnotations.length == 0) {
      LOGGER.info("Skipping documents loading: no @Dataset annotation available.");
      return Collections.emptyList();
    }
    return Stream.of(datasetAnnotations).map(annotation -> (Dataset) annotation)
        .filter(datasetAnnotation -> datasetAnnotation.documents() != null
            && !datasetAnnotation.documents().isEmpty())
        .map(datasetAnnotation -> {
          LOGGER.info("Loading documents using '{}'", datasetAnnotation.settings());
          try (
              final InputStream jsonStream = Thread.currentThread().getContextClassLoader()
                  .getResourceAsStream(datasetAnnotation.documents());
              final JsonReader jsonReader = Json.createReader(jsonStream);) {
            return jsonReader.readObject();
          } catch (IOException e) {
            e.printStackTrace();
            fail("Failed to read file " + datasetAnnotation.documents() + ": " + e.getMessage());
          }
          return null;
        }).collect(Collectors.toList());
  }


  /**
   * Deletes all indices and their data.
   */
  private void deleteIndices(final List<JsonObject> allSettings) {
    allSettings.stream().flatMap(settings -> settings.keySet().stream())
        .filter(indexName -> client.indexExists(indexName)).forEach(indexName -> {
          // force flush on index
          client.flush(indexName);
          // delete index
          client.deleteIndex(indexName);
          // make sure index is removed
          while (client.indexExists(indexName)) {
            try {
              Thread.currentThread().sleep(TimeUnit.SECONDS.toMillis(1));
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
            }
          }

        });
  }

  /**
   * Create the indices from the resource specified in the given {@link Dataset#settings()}
   * attribute.
   * 
   * @param dataset the {@link Dataset} specification
   * @throws IOException if an I/O error occurs while closing the {@link InputStream} for the
   *         dataset location.
   * @throws InterruptedException may occur while thread is sleeping, to give time to Elasticsearch
   *         to actually index the data that was sent.
   */
  private void createIndices(final List<JsonObject> allSettings)
      throws IOException, InterruptedException {
    try {
      allSettings.stream().flatMap(settings -> settings.entrySet().stream()).forEach(indexEntry -> {
        final String indexName = indexEntry.getKey();
        // prepare the index creation
        // update the index with the specific configuration
        final JsonObject indexSettings = (JsonObject) indexEntry.getValue();
        if (indexSettings.getJsonObject("settings") == null) {
          LOGGER.info("No 'settings' entry found for index {} in dataset file.", indexName);
        }
        if (indexSettings.getJsonObject("mappings") == null) {
          LOGGER.info("No 'mappings' entry found for index {} in dataset file.", indexName);
        }
        // create the index with all the config
        LOGGER.debug("Creating index {} with settings: {}", indexName,
            indexEntry.getValue().toString());
        client.createIndex(indexName, indexEntry.getValue().toString());
      });

    } finally {
      LOGGER.info("Indices configuration done.");
    }
  }

  /**
   * Indexes the documents from the resource specified in the given {@link Dataset#documents()}
   * attribute, and waits until all elements have been indexed (ie, index size == number of
   * documents).
   * 
   * @param dataset the {@link Dataset} specification
   * @throws IOException if an I/O error occurs while closing the {@link InputStream} for the
   *         dataset location.
   * @throws InterruptedException may occur while thread is sleeping, to give time to Elasticsearch
   *         to actually index the data that was sent.
   */
  private void indexDocuments(final List<JsonObject> allDocuments)
      throws IOException, InterruptedException {
    LOGGER.info("Indexing documents using '{}'", allDocuments);
    final Map<String, AtomicInteger> docCounts = new HashMap<>();
    try {
      allDocuments.stream()
          // convert each entry into a JsonObject
          .flatMap(docs -> ((JsonArray) docs.get("documents")).stream())
          .map(doc -> (JsonObject) doc)
          // for each 'document'
          .forEach(doc -> {
            // the index in which the document should be stored
            final JsonArray indices = getIndices(doc);
            // the document to store
            final String data = getDocumentData(doc);
            // add the document into the indices
            indices.parallelStream().map(index -> (JsonObject) index).forEach(index -> {
              final String indexName = index.getString("indexName");
              final String type = index.getString("type");
              final String id = index.getString("id");
              LOGGER.debug("Adding document at {}/{}/{}: {}", indexName, type, id, data);
              final IndexDocumentResponse indexResponse = client.index(indexName, type, id, data);
              Assertions.assertThat(indexResponse.isCreated()).isEqualTo(true);
              if (!docCounts.containsKey(indexName)) {
                docCounts.put(indexName, new AtomicInteger(1));
              } else {
                docCounts.get(indexName).incrementAndGet();
              }
            });
          });
    } finally {
      // give Elasticsearch a bit of time to actually index all the data that was sent
      // (should be 1s or less)
      if (!docCounts.isEmpty()) {
        docCounts.entrySet().stream().forEach(entry -> ESAssertions.assertThat(client)
            .hasIndexSize(entry.getKey(), entry.getValue().get()));
      }
      LOGGER.info("Loading dataset done.");

    }
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
  private String getDocumentData(final JsonObject doc) {
    final Entry<String, JsonValue> dataEntry =
        doc.entrySet().stream().filter(entry -> entry.getKey().equals("data")).findFirst()
            .orElseThrow(() -> new InvalidDatasetException(
                "Dataset did not contain an 'data' entry in the document JSON object."));
    final JsonObject data = (JsonObject) dataEntry.getValue();
    return data.toString();
  }

  private static Map<String, ?> toMap(final JsonObject jsonObject) {
    return jsonObject.entrySet().stream().collect(DatasetRule.toMapCollector());
  }

  private static Collector<Map.Entry<String, JsonValue>, ?, Map<String, Object>> toMapCollector() {
    return Collector.of(HashMap::new,
        (resultMap, entry) -> resultMap.put(entry.getKey(), valueOf(entry.getValue())),
        DatasetRule::merge);
  }

  private static Object valueOf(final JsonValue value) {
    switch (value.getValueType()) {
      case FALSE:
        return false;
      case NULL:
        return null;
      case STRING:
        return ((JsonString) value).getString();
      case TRUE:
        return true;
      case ARRAY:
        final JsonArray jsonArray = ((JsonArray) value);
        final List<Object> result = new ArrayList<>();
        jsonArray.iterator().forEachRemaining(item -> result.add(valueOf(item)));
        return result;
      case OBJECT:
        return toMap((JsonObject) value);
      case NUMBER:
        final JsonNumber number = (JsonNumber) value;
        if (number.isIntegral()) {
          return number.longValue(); // or other methods to get integral value
        } else {
          return number.doubleValue(); // or other methods to get decimal number value
        }
      default:
        throw new ConversionException(
            "Unexpected Json value of type '" + value.getValueType().name() + "'");
    }
  }

  /**
   * Merges the given {@code left} and {@code right} maps into a single, new {@link Map}.
   * 
   * @param left the left {@link Map} to merge
   * @param right the right {@link Map} to merge
   * @return the combination of {@code left} and {@code right} maps.
   */
  static Map<String, Object> merge(final Map<String, Object> left,
      final Map<String, Object> right) {
    final Map<String, Object> retVal = new HashMap<>();
    left.keySet().stream().forEach((key) -> retVal.put(key, left.get(key)));
    right.keySet().stream().forEach((key) -> retVal.put(key, right.get(key)));
    return retVal;
  }

}
