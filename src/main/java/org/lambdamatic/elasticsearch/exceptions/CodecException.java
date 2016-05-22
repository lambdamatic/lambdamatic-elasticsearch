
package org.lambdamatic.elasticsearch.exceptions;

/**
 * {@link CodecException} are thrown when conversion of a Java domain instance into an Elasticsearch/Lucene
 * document fails.
 * 
 * @author Xavier Coulon
 *
 */
public class CodecException extends RuntimeException {

  /** serialVersionUID. */
  private static final long serialVersionUID = 7216008533037657142L;

  /**
   * Constructor without an underlying cause {@link Exception}.
   * 
   * @param message the exception message
   */
  public CodecException(final String message) {
    super(message);
  }

  /**
   * Constructor with an underlying cause {@link Exception}.
   * 
   * @param message the contextual message
   * @param cause the underlying cause
   */
  public CodecException(final String message, final Exception cause) {
    super(message, cause);
  }

}
