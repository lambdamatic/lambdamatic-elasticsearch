/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.internal.elasticsearch.clientdsl.responses;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.lambdamatic.elasticsearch.exceptions.CodecException;
import org.lambdamatic.internal.elasticsearch.clientdsl.responses.GetIndexMappingsResponse.GetIndexMappingsResponseDeserializer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Response to a request to get the typeMappings of an index.
 */
@JsonDeserialize(using = GetIndexMappingsResponseDeserializer.class)
public class GetIndexMappingsResponse {

  private final Map<String, IndexMapping> indexMappings = new HashMap<>();

  void addIndexMapping(final String indexName, final IndexMapping indexMapping) {
    this.indexMappings.put(indexName, indexMapping);
  }

  public IndexMapping getIndexMappings(final String indexName) {
    return this.indexMappings.get(indexName);
  }

  @JsonDeserialize(using = IndexMappingDeserializer.class)
  public static class IndexMapping {

    private final Map<String, TypeMapping> typeMappings = new HashMap<>();

    void addTypeMapping(final String typeName, final TypeMapping typeMapping) {
      this.typeMappings.put(typeName, typeMapping);
    }

    public TypeMapping getTypeMapping(final String typeName) {
      return this.typeMappings.get(typeName);
    }

  }

  @JsonDeserialize(using = TypeMappingDeserializer.class)
  public static class TypeMapping {

    private final Map<String, PropertyMapping> propertyMappings = new HashMap<>();

    void addPropertyMapping(final String propertyName, final PropertyMapping propertyMapping) {
      this.propertyMappings.put(propertyName, propertyMapping);
    }

    public PropertyMapping getProperty(final String propertyName) {
      return this.propertyMappings.get(propertyName);
    }

    public Map<String, PropertyMapping> getProperties() {
      return this.propertyMappings;
    }

  }

  public static class PropertyMapping {

    @JsonProperty("type")
    private String type;

    @JsonProperty("properties")
    private Map<String, PropertyMapping> propertyMappings;

    /**
     * @return the type of the simple property
     */
    public String getType() {
      return type;
    }

    /**
     * @param propertyName
     * @return the nest property if this property is complex
     */
    public PropertyMapping getProperty(final String propertyName) {
      return propertyMappings.get(propertyName);
    }

    public boolean isEmpty() {
      return this.propertyMappings == null || this.propertyMappings.isEmpty();
    }

  }

  public static class GetIndexMappingsResponseDeserializer
      extends JsonDeserializer<GetIndexMappingsResponse> {

    @Override
    public GetIndexMappingsResponse deserialize(final JsonParser parser,
        final DeserializationContext ctxt) throws IOException, JsonProcessingException {
      final GetIndexMappingsResponse response = new GetIndexMappingsResponse();
      final TreeNode indexsMappingsTree = parser.readValueAsTree();
      indexsMappingsTree.fieldNames().forEachRemaining(indexName -> {
        final TreeNode indexMappingNode = indexsMappingsTree.get(indexName);
        try (final JsonParser indexMappingParser = indexMappingNode.traverse(new ObjectMapper())) {
          final IndexMapping indexMapping = indexMappingParser.readValueAs(IndexMapping.class);
          response.addIndexMapping(indexName, indexMapping);
        } catch (IOException e) {
          throw new CodecException(
              "Failed to parse response fragment into an instance of " + IndexMapping.class, e);
        }
      });
      return response;
    }

  }

  public static class IndexMappingDeserializer extends JsonDeserializer<IndexMapping> {

    @Override
    public IndexMapping deserialize(final JsonParser p, final DeserializationContext ctxt)
        throws IOException, JsonProcessingException {
      final IndexMapping indexMapping = new IndexMapping();
      final TreeNode indexMappingTree = p.readValueAsTree();
      final TreeNode mappingsNode = indexMappingTree.get("mappings");
      mappingsNode.fieldNames().forEachRemaining(typeName -> {
        final TreeNode typeMappingNode = mappingsNode.get(typeName);
        try (final JsonParser typeMappingParser = typeMappingNode.traverse(new ObjectMapper())) {
          final TypeMapping typeMapping = typeMappingParser.readValueAs(TypeMapping.class);
          indexMapping.addTypeMapping(typeName, typeMapping);
        } catch (IOException e) {
          throw new CodecException(
              "Failed to parse response fragment into an instance of " + TypeMapping.class, e);
        }
      });
      return indexMapping;
    }

  }

  public static class TypeMappingDeserializer extends JsonDeserializer<TypeMapping> {

    @Override
    public TypeMapping deserialize(final JsonParser p, final DeserializationContext ctxt)
        throws IOException, JsonProcessingException {
      final TypeMapping typeMapping = new TypeMapping();
      final TreeNode typeMappingTree = p.readValueAsTree();
      final TreeNode propertiesNode = typeMappingTree.get("properties");
      if (propertiesNode != null) {
        propertiesNode.fieldNames().forEachRemaining(propertyName -> {
          final TreeNode typeMappingNode = propertiesNode.get(propertyName);
          try (final JsonParser typeMappingParser = typeMappingNode.traverse(new ObjectMapper())) {
            final PropertyMapping propertyMapping =
                typeMappingParser.readValueAs(PropertyMapping.class);
            typeMapping.addPropertyMapping(propertyName, propertyMapping);
          } catch (IOException e) {
            throw new CodecException(
                "Failed to parse response fragment into an instance of " + PropertyMapping.class,
                e);
          }
        });
      }
      return typeMapping;
    }

  }
}
