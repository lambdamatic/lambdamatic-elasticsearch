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

import java.text.MessageFormat;

/**
 * {@link RuntimeException} thrown when a document was not found.
 */
public class DocumentNotFoundException extends RuntimeException {

  private static final long serialVersionUID = -661372525220208583L;

  private static final String MESSAGE = "No document with id ''{2}'' and type ''{1}'' available in index ''{0}''";
  
  /**
   * Constructor.
   * 
   * @param indexName the name of the index 
   * @param type the document type
   * @param documentId the document id
   */
  public DocumentNotFoundException(final String indexName, final String type, final String documentId) {
    super(MessageFormat.format(MESSAGE, indexName, type, documentId));
  }
  
  
  
}
