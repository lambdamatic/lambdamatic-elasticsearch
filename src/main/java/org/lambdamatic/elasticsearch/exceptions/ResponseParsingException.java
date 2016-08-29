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

package org.lambdamatic.elasticsearch.exceptions;

/**
 * {@link RuntimeException} thrown when the parsing of a response failed. 
 */
public class ResponseParsingException extends RuntimeException {

  private static final long serialVersionUID = 6188847683010769396L;

  /**
   * Constructor.
   * 
   * @param message the error message
   * @param cause the underlying cause
   */
  public ResponseParsingException(final String message, final Exception cause) {
    super(message, cause);
  }


}
