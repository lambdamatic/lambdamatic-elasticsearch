/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.elasticsearch.exceptions;

import java.io.IOException;

import org.lambdamatic.internal.elasticsearch.clientdsl.Client;

/**
 * {@link RuntimeException} thrown when the underlying {@link Client} fails to submit a
 * request/retrieve the response with an IOException.
 */
public class ClientIOException extends RuntimeException {

  private static final long serialVersionUID = 9175411149588987759L;

  /**
   * Constructor.
   * 
   * @param message the error message
   * @param cause the underlying cause
   */
  public ClientIOException(final String message, final IOException cause) {
    super(message, cause);
  }

}
