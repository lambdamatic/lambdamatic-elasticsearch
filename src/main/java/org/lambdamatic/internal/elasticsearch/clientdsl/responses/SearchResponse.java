/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.internal.elasticsearch.clientdsl.responses;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * The response to a search request.
 * 
 * @param <D> the type of the domain document.
 */
public class SearchResponse<D> {

  @JsonProperty("hits")
  private SearchHits searchHits;

  public List<SearchHit> getSearchHits() {
    return Arrays.asList(this.searchHits.getHits());
  }

  public long getTotalHits() {
    return this.searchHits.getTotal();
  }

  public static class SearchHits {

    @JsonProperty("total")
    private long total;

    @JsonProperty("hits")
    private SearchHit[] hits;

    public long getTotal() {
      return this.total;
    }

    public SearchHit[] getHits() {
      return this.hits;
    }
  }

  public static class SearchHit {

    @JsonProperty("_index")
    private String indexName;

    @JsonProperty("_type")
    private String type;

    @JsonProperty("_id")
    private String id;

    @JsonProperty("_source")
    private JsonNode source;

    public String getIndexName() {
      return this.indexName;
    }

    public String getType() {
      return this.type;
    }

    public String getId() {
      return this.id;
    }

    public JsonNode getSource() {
      return this.source;
    }

  }

}
