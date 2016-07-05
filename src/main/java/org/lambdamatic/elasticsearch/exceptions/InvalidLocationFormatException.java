package org.lambdamatic.elasticsearch.exceptions;

import org.lambdamatic.elasticsearch.annotations.GeoPoint;

/**
 * {@link RuntimeException} thrown when the type annotated with {@link GeoPoint} does not fulfill
 * the contract.
 * 
 */
public class InvalidLocationFormatException extends RuntimeException {

  private static final long serialVersionUID = -700846455007734266L;

  /**
   * Constructor.
   * @param message the error message
   */
  public InvalidLocationFormatException(final String message) {
    super(message);
  }

  /**
   * Constructor.
   * @param message the error message
   * @param cause the underlying cause
   */
  public InvalidLocationFormatException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
