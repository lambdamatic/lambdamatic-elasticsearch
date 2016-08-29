/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.elasticsearch.exceptions;

import org.lambdamatic.internal.elasticsearch.clientdsl.Client;
import org.lambdamatic.internal.elasticsearch.clientdsl.responses.ErrorResponse;

/**
 * {@link RuntimeException} thrown when the underlying {@link Client} receives an error response on a request.
 */
public class ClientResponseException extends RuntimeException {

  private static final long serialVersionUID = 9175411149588987759L;

  private final ErrorResponse errorResponse;
  
  /**
   * Constructor.
   * 
   * @param message the error message
   * @param errorResponse the {@link ErrorResponse} received by the client
   */
  public ClientResponseException(final String message, final ErrorResponse errorResponse) {
    super(message, new ErrorException(errorResponse));
    this.errorResponse = errorResponse;
  }
  
  public ErrorResponse getErrorResponse() {
    return errorResponse;
  }

  /**
   * Inner exception to wrap the {@link ErrorResponse} passed in the {@link ClientResponseException}
   * constructor, so that it can be set as the root cause and displayed in the stacktrace.
   */
  static class ErrorException extends Exception {

    public ErrorException(final ErrorResponse errorResponse) {
      super(errorResponse.getError().getReason());
    }
    
  }
}
