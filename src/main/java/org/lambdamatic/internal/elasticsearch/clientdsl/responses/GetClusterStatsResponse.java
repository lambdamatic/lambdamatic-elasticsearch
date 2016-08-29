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

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 
 */
public class GetClusterStatsResponse {

  @JsonProperty("timestamp")
  private String timestamp;
  
  @JsonProperty("cluster_name")
  private String clusterName;

  @JsonProperty("status")
  private String status;
  
  @JsonProperty("indices")
  private IndicesStats indicesStats;
  
  public String getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }

  public String getClusterName() {
    return clusterName;
  }

  public void setClusterName(String clusterName) {
    this.clusterName = clusterName;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public IndicesStats getIndicesStats() {
    return indicesStats;
  }

  public void setIndicesStats(IndicesStats indices) {
    this.indicesStats = indices;
  }

  public static class IndicesStats {
    
    @JsonProperty("count")
    private long count;
    
    @JsonProperty("docs")
    private DocsStats docs;

    public long getCount() {
      return count;
    }

    public void setCount(long count) {
      this.count = count;
    }

    public DocsStats getDocs() {
      return docs;
    }

    public void setDocs(DocsStats docs) {
      this.docs = docs;
    }
    
  }
  
  public static class DocsStats {
    
    @JsonProperty("count")
    private long count;
    
    @JsonProperty("deleted")
    private long deleted;

    public long getCount() {
      return count;
    }

    public void setCount(long count) {
      this.count = count;
    }

    public long getDeleted() {
      return deleted;
    }

    public void setDeleted(long deleted) {
      this.deleted = deleted;
    }
   
  }
}
