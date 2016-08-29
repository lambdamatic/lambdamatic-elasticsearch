/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.internal.elasticsearch.clientdsl.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The response message when an error occurred.
 */
public class ErrorResponse {

  @JsonProperty("status")
  private int status;

  @JsonProperty("error")
  private Error error;

  public int getStatus() {
    return status;
  }

  public Error getError() {
    return error;
  }

  public static class Error {

    @JsonProperty("type")
    private String type;

    @JsonProperty("reason")
    private String reason;

    @JsonProperty("resource.type")
    private String resourceType;

    @JsonProperty("resource.id")
    private String resourceId;

    @JsonProperty("index_uuid")
    private String indexId;

    @JsonProperty("index")
    private String indexName;

    public String getType() {
      return type;
    }

    public String getReason() {
      return reason;
    }

    public String getResourceType() {
      return resourceType;
    }

    public String getResourceId() {
      return resourceId;
    }

    public String getIndexId() {
      return indexId;
    }

    public String getIndexName() {
      return indexName;
    }

  }

}
