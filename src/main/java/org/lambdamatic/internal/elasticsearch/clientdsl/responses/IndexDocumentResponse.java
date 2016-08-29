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
 * 
 */
public class IndexDocumentResponse {

  @JsonProperty(value = "_id")
  private String id;
  
  @JsonProperty(value = "created")
  private boolean created;
  

  public String getId() {
    return this.id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public boolean isCreated() {
    return this.created;
  }

  

}
