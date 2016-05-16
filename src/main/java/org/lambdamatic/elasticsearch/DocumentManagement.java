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

package org.lambdamatic.elasticsearch;

import java.util.function.Consumer;

/**
 * Interface for all operations related to document management.
 * @param <D> The document type.
 */
public interface DocumentManagement<D> {

  /**
   * Adds the given document in the index
   * 
   * @param document the document to add
   * @return A <a href=
   *         "https://github.com/reactive-streams/reactive-streams-jvm/blob/v1.0.0/README.md">
   *         Reactive Streams</a> {@link Publisher} for the {@link IndexResponse} returned by the
   *         underlying {@link Client}.
   * TODO: return a simpler type to handle the response. 
   */
  //public Publisher<IndexResponse> index(D document);

  /**
   * The gets the document identified by the given {@code documentId} from the index.
   * 
   * @param documentId the id of the document to get
   * @return the instance of document whose id matches the given {@code documentId},
   *         <code>null</code> if no document was found. 
   */
  public D get(String documentId);

  /**
   * The gets the document identified by the given {@code documentId} from the index.
   * 
   * @param documentId the id of the document to get
   * @param onSuccess the handler to call when the operation succeeds
   * @param onError the handler to call if the operation failed 
   * 
   */
  public void asyncGet(String documentId, Consumer<D> onSuccess, Consumer<Throwable> onError);
  
  /**
   * The delete API allows one to delete a typed JSON document from a specific index based on its
   * id.
   * 
   * @param documentId the id of the document to delete
   * 
   * @return A <a href=
   *         "https://github.com/reactive-streams/reactive-streams-jvm/blob/v1.0.0/README.md">
   *         Reactive Streams</a> {@link Publisher} for the {@link DeleteResponse} returned by the
   *         underlying {@link Client}.
   * TODO: return a simpler type to handle the response. 
   */
  //public Publisher<DeleteResponse> delete(Object documentId);

  
}
