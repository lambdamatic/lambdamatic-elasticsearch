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

package org.lambdamatic.internal.elasticsearch;

import java.text.MessageFormat;

/**
 * A {@link RuntimeException} to throw when there is a mapping issue.
 */
public class MappingException extends RuntimeException {

  /** serialVersionUID. */
  private static final long serialVersionUID = 6317260821066778441L;

  /**
   * Default constructor.
   * @param message the exception message
   */
  public MappingException(final String message) {
    super(message);
  }

  /**
   * Constructor with message arguments.
   * @param messagePattern the exception message (as a pattern)
   * @param args message arguments
   * @see MessageFormat#format(String, Object...)
   */
  public MappingException(final String messagePattern, final Object... args) {
    super(MessageFormat.format(messagePattern, args));
  }
  
}
