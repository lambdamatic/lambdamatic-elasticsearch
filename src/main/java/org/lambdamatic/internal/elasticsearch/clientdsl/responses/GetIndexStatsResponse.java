/*******************************************************************************
 * Copyright (c) 2016 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.internal.elasticsearch.clientdsl.responses;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * JSON POJO for the response to the request to get indices stats.
 */
public class GetIndexStatsResponse {

  @JsonProperty("indices")
  private Map<String, IndiceStats> indicesStats;

  public IndiceStats getIndex(final String indexName) {
    return indicesStats.get(indexName);
  }
  
  public static class IndiceStats {

    @JsonProperty("total")
    private Total total;
    
    public Total getTotal() {
      return total;
    }
    
  }
  
  public static class Total {

    @JsonProperty("docs")
    private Docs docs;
    
    public Docs getDocs() {
      return docs;
    }
    
  }
  
  public static class Docs {

    @JsonProperty("count")
    private long count;
    
    public long getCount() {
      return count;
    }
    
  }
  
  
}
