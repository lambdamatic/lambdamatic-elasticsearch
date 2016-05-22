
package org.lambdamatic.elasticsearch.exceptions;

import java.beans.IntrospectionException;

/**
 * {@link DomainTypeException} are thrown when the introspection of a given Domain type fails. 
 * This is a {@link RuntimeException} wrapper for checked exceptions such as {@link IntrospectionException}. 
 * 
 * @author Xavier Coulon
 *
 */
public class DomainTypeException extends RuntimeException {

  /** serialVersionUID. */
  private static final long serialVersionUID = 7216008533037657142L;

  /**
   * Constructor.
   * 
   * @param message the contextual message
   */
  public DomainTypeException(final String message) {
    super(message);
  }

  /**
   * Constructor with an underlying cause {@link Exception}.
   * 
   * @param message the contextual message
   * @param cause the underlying cause
   */
  public DomainTypeException(final String message, final Exception cause) {
    super(message, cause);
  }
  
}
