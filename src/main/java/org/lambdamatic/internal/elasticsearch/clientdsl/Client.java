/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.internal.elasticsearch.clientdsl;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpHost;
import org.apache.http.entity.ByteArrayEntity;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.ResponseListener;
import org.elasticsearch.client.RestClient;
import org.lambdamatic.elasticsearch.exceptions.ClientIOException;
import org.lambdamatic.elasticsearch.exceptions.ClientResponseException;
import org.lambdamatic.elasticsearch.exceptions.ResponseParsingException;
import org.lambdamatic.internal.elasticsearch.clientdsl.responses.ErrorResponse;
import org.lambdamatic.internal.elasticsearch.clientdsl.responses.GetClusterStatsResponse;
import org.lambdamatic.internal.elasticsearch.clientdsl.responses.GetDocumentResponse;
import org.lambdamatic.internal.elasticsearch.clientdsl.responses.GetIndexMappingsResponse;
import org.lambdamatic.internal.elasticsearch.clientdsl.responses.GetIndexStatsResponse;
import org.lambdamatic.internal.elasticsearch.clientdsl.responses.GetIndicesResponse;
import org.lambdamatic.internal.elasticsearch.clientdsl.responses.IndexDocumentResponse;
import org.lambdamatic.internal.elasticsearch.clientdsl.responses.SearchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Higher-level client to perform operations on Elasticsearch.
 */
public class Client {

  /** The usual Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(Client.class);

  private final RestClient client;

  private final JsonFactory jsonFactory;

  private Client(RestClient client) {
    this.client = client;
    this.jsonFactory = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).getFactory();
  }

  public static Client connectTo(final HttpHost... hosts) {
    return new Client(RestClient.builder(hosts).build());
  }

  public <T> IndexDocumentResponse index(final String indexName, final String type,
      final String documentId, final String documentSource) {
    try {
      final PathBuilder pathBuilder = new PathBuilder().append(indexName).append(type);
      final Map<String, String> params = new HashMap<>();
      if (documentId != null) {
        pathBuilder.append(documentId);
        // use the "op_type=create" argument to obtain a "put-if-absent" behaviour. Will fail if
        // a document with the same id already exists
        params.put("op_type", "create");
      }
      final Response response = client.performRequest("PUT", pathBuilder.build(), params,
          new ByteArrayEntity(documentSource.getBytes()));
      // something wrong happened
      // document id was allocated by the server and must be set in the given domain object
      return readResponse(this.jsonFactory, response, IndexDocumentResponse.class);
    } catch (ResponseException e) {
      throw new ClientResponseException("Failed to index document",
          readResponse(this.jsonFactory, e.getResponse(), ErrorResponse.class));
    } catch (IOException e) {
      throw new ClientIOException("Failed to index document", e);
    }
  }

  public <T> void index(final String indexName, final String type, final String documentId,
      final String documentSource, final ResponseListener responseListener) {
    final PathBuilder pathBuilder = new PathBuilder().append(indexName).append(type);
    final Map<String, String> params = new HashMap<>();
    if (documentId != null) {
      pathBuilder.append(documentId);
      // use the "op_type=create" argument to obtain a "put-if-absent" behaviour. Will fail if
      // a document with the same id already exists
      params.put("op_type", "create");
    }
    client.performRequest("PUT", pathBuilder.build(), params,
        new ByteArrayEntity(documentSource.getBytes()), responseListener);
  }

  public GetClusterStatsResponse getClusterStats() {
    try {
      final PathBuilder pathBuilder = new PathBuilder().append("_cluster").append("stats");
      final Map<String, String> params = new HashMap<>();
      final Response response = client.performRequest("GET", pathBuilder.build(), params);
      return readResponse(this.jsonFactory, response, GetClusterStatsResponse.class);
    } catch (ResponseException e) {
      throw new ClientResponseException("Failed to retrieve cluster stats",
          readResponse(this.jsonFactory, e.getResponse(), ErrorResponse.class));
    } catch (IOException e) {
      throw new ClientIOException("Failed to retrieve cluster stats", e);
    }

  }

  public GetIndexStatsResponse getIndexStats(final String indexName) {
    try {
      final PathBuilder pathBuilder = new PathBuilder().append(indexName).append("_stats");
      final Map<String, String> params = new HashMap<>();
      final Response response = client.performRequest("GET", pathBuilder.build(), params);
      return readResponse(this.jsonFactory, response, GetIndexStatsResponse.class);
    } catch (ResponseException e) {
      throw new ClientResponseException("Failed to retrieve index stats",
          readResponse(this.jsonFactory, e.getResponse(), ErrorResponse.class));
    } catch (IOException e) {
      throw new ClientIOException("Failed to retrieve index stats", e);
    }
  }

  public GetIndexMappingsResponse getIndexMappings(final String indexName, final String type) {
    try {
      final PathBuilder pathBuilder =
          new PathBuilder().append(indexName).append("_mapping").append(type);
      final Map<String, String> params = new HashMap<>();
      final Response response = client.performRequest("GET", pathBuilder.build(), params);
      return readResponse(this.jsonFactory, response, GetIndexMappingsResponse.class);
    } catch (ResponseException e) {
      throw new ClientResponseException("Failed to retrieve index mappings",
          readResponse(this.jsonFactory, e.getResponse(), ErrorResponse.class));
    } catch (IOException e) {
      throw new ClientIOException("Failed to retrieve index mappings", e);
    }
  }

  public GetDocumentResponse getDocument(final String indexName, final String type,
      final String id) {
    try {
      final PathBuilder pathBuilder = new PathBuilder().append(indexName).append(type).append(id);
      final Map<String, String> params = new HashMap<>();
      final Response response = client.performRequest("GET", pathBuilder.build(), params);
      return readResponse(this.jsonFactory, response, GetDocumentResponse.class);
    } catch (ResponseException e) {
      throw new ClientResponseException("Failed to get document",
          readResponse(this.jsonFactory, e.getResponse(), ErrorResponse.class));
    } catch (IOException e) {
      throw new ClientIOException("Failed to get document", e);
    }
  }

  public void getDocument(final String indexName, final String type, final String id,
      final ResponseListener listener) {
    final PathBuilder pathBuilder = new PathBuilder().append(indexName).append(type).append(id);
    final Map<String, String> params = new HashMap<>();
    client.performRequest("GET", pathBuilder.build(), params, listener);
  }

  public GetIndicesResponse getIndices() {
    try {
      final PathBuilder pathBuilder = new PathBuilder().append("*").append("_alias");
      final Map<String, String> params = new HashMap<>();
      final Response response = client.performRequest("GET", pathBuilder.build(), params);
      return readResponse(this.jsonFactory, response, GetIndicesResponse.class);
    } catch (ResponseException e) {
      throw new ClientResponseException("Failed to retrieve the indices",
          readResponse(this.jsonFactory, e.getResponse(), ErrorResponse.class));
    } catch (IOException e) {
      throw new ClientIOException("Failed to retrieve the indices", e);
    }
  }

  public void createIndex(final String indexName, final Map<String, Object> settings) {
    // convert the settings into a JSON Object
    final ObjectMapper objectMapper = new ObjectMapper();
    final Map<String, Object> indexConfig = new HashMap<>();
    if (settings != null) {
      indexConfig.put("settings", settings);
      try {
        createIndex(indexName, objectMapper.writeValueAsString(indexConfig));
      } catch (JsonProcessingException e) {
        // FIXME: create a specific type of exception for this ?
        throw new RuntimeException(e);
      }
    } else {
      LOGGER.warn("No settings for index {}. Skipping index creation.", indexName);
    }
  }

  public void createIndexType(final String indexName, final String type,
      final Map<String, Object> mappings) {
    // convert the settings into a JSON Object
    final ObjectMapper objectMapper = new ObjectMapper();
    final Map<String, Object> indexConfig = new HashMap<>();
    if (mappings != null) {
      final Map<String, Object> typeMapping = new HashMap<>();
      typeMapping.put(type, mappings);
      indexConfig.put("mappings", typeMapping);
      try {
        createIndex(indexName, objectMapper.writeValueAsString(indexConfig));
      } catch (JsonProcessingException e) {
        // FIXME: create a specific type of exception for this ?
        throw new RuntimeException(e);
      }
    } else {
      LOGGER.warn("No settings for type {} in index {}. Skipping index type creation.", type,
          indexName);
    }
  }

  public void createIndex(final String indexName, final String indexConfig) {
    try {
      final PathBuilder pathBuilder = new PathBuilder().append(indexName);
      final Map<String, String> params = new HashMap<>();
      client.performRequest("PUT", pathBuilder.build(), params,
          new ByteArrayEntity(indexConfig.getBytes()));
    } catch (ResponseException e) {
      throw new ClientResponseException("Failed to create index",
          readResponse(this.jsonFactory, e.getResponse(), ErrorResponse.class));
    } catch (IOException e) {
      throw new ClientIOException("Failed to create index", e);
    }
  }


  public void deleteAllIndices() {
    LOGGER.warn("Deleting all indices !");
    deleteIndex("*");
  }

  public void deleteIndex(final String indexName) {
    try {
      final PathBuilder pathBuilder = new PathBuilder().append(indexName);
      final Map<String, String> params = new HashMap<>();
      final Response deleteIndexResponse = client.performRequest("DELETE", pathBuilder.build(), params);
      LOGGER.debug(deleteIndexResponse.toString());
    } catch (ResponseException e) {
      throw new ClientResponseException("Failed to delete index",
          readResponse(this.jsonFactory, e.getResponse(), ErrorResponse.class));
    } catch (IOException e) {
      throw new ClientIOException("Failed to delete index", e);
    }
  }
  
  public void flush(final String indexName) {
    try {
      final PathBuilder pathBuilder = new PathBuilder().append(indexName).append("_flush");
      client.performRequest("POST", pathBuilder.build());
    } catch (IOException e) {
      throw new ClientIOException("Failed to flush index '" + indexName , e);
    }
  }

  /**
   * Submits a request to check if an index with the given {@code indexName} exists.
   * 
   * @param indexName the name of the index to look-up
   * @return <code>true</code> if the index exists, <code>false</code> otherwise
   */
  public boolean indexExists(final String indexName) {
    try {
      final PathBuilder pathBuilder = new PathBuilder().append(indexName);
      final Response indexExistsResponse = client.performRequest("HEAD", pathBuilder.build());
      if (indexExistsResponse.getStatusLine().getStatusCode() == 200) {
        return true;
      }
      return false;
    } catch (ResponseException e) {
      throw new ClientResponseException("Failed to check if index '" + indexName + "' exists",
          readResponse(this.jsonFactory, e.getResponse(), ErrorResponse.class));
    } catch (IOException e) {
      throw new ClientIOException("Failed to check if index '" + indexName + "' exists", e);
    }
  }

  public <D> SearchResponse<D> search(final String indexName, final String type,
      final String requestBody) {
    try {
      final PathBuilder pathBuilder =
          new PathBuilder().append(indexName).append(type).append("_search");
      final Map<String, String> params = new HashMap<>();
      if (LOGGER.isDebugEnabled()) {
        final String indented = formatJsonDocument(requestBody);
        LOGGER.debug("Sending search request on {}:\n{}", pathBuilder.build(), indented);

      }
      final Response response = client.performRequest("GET", pathBuilder.build(), params,
          new ByteArrayEntity(requestBody.getBytes()));
      return readResponse(this.jsonFactory, response, SearchResponse.class);
    } catch (ResponseException e) {
      throw new ClientResponseException("Failed to search documents",
          readResponse(this.jsonFactory, e.getResponse(), ErrorResponse.class));
    } catch (IOException e) {

      throw new ClientIOException("Failed to delete index", e);
    }
  }

  private static String formatJsonDocument(final String requestBody)
      throws IOException, JsonParseException, JsonMappingException, JsonProcessingException {
    final ObjectMapper mapper = new ObjectMapper();
    final Object json = mapper.readValue(requestBody, Object.class);
    final String indented = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
    return indented;
  }

  private static String formatJsonDocument(final InputStream requestBodyStream)
      throws IOException, JsonParseException, JsonMappingException, JsonProcessingException {
    final ObjectMapper mapper = new ObjectMapper();
    final Object json = mapper.readValue(requestBodyStream, Object.class);
    final String indented = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
    return indented;
  }

  public static <T> T readResponse(final JsonFactory jsonFactory, final Response response,
      final Class<T> responseType) {
    try {
      try (final InputStream responseBodyStream = response.getEntity().getContent()) {
        if (LOGGER.isTraceEnabled()) {
          final String responseBody = formatJsonDocument(responseBodyStream);
          LOGGER.trace("Parsing response body:\n{}", responseBody);
          return jsonFactory.createParser(responseBody).readValueAs(responseType);
        }
        return jsonFactory.createParser(responseBodyStream).readValueAs(responseType);
      }
    } catch (UnsupportedOperationException | IOException e) {
      throw new ResponseParsingException("Failed to parse response body", e);
    }
  }

  public static <T> T readResponse(final JsonFactory jsonFactory, final String responseBody,
      final Class<T> responseType) {
    try {
      return jsonFactory.createParser(responseBody).readValueAs(responseType);
    } catch (UnsupportedOperationException | IOException e) {
      throw new ResponseParsingException("Failed to parse response body", e);
    }
  }

  static class PathBuilder {

    private static final char SEPARATOR = '/';

    private final StringBuilder path;

    PathBuilder() {
      path = new StringBuilder();
    }

    PathBuilder append(final String fragment) {
      this.path.append(SEPARATOR).append(fragment);
      return this;
    }

    String build() {
      return this.path.toString();
    }

  }



}
