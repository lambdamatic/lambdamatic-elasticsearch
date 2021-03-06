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

package org.lambdamatic.internal.elasticsearch.testutils;

/**
 * Exception thrown when a data set is invalid.
 */
public class InvalidDatasetException extends RuntimeException {

  public InvalidDatasetException(final String message) {
    super(message);
  }

}
