package org.lambdamatic.elasticsearch.exceptions;

/**
 * @author xcoulon.
 */
public class InvalidLocationFormatException extends RuntimeException {

  public InvalidLocationFormatException(final String message) {
    super(message);
  }

  public InvalidLocationFormatException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
