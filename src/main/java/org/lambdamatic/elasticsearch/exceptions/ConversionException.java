
package org.lambdamatic.elasticsearch.exceptions;

import org.apache.lucene.search.Query;
import org.lambdamatic.elasticsearch.search.SearchExpression;

/**
 * {@link ConversionException} are thrown when conversion of a Java domain instance into an Lucene
 * document fails, or when an Elasticsearch/Lucene {@link Query} fails to be built from a
 * {@link SearchExpression}.
 * 
 * @author Xavier Coulon
 *
 */
public class ConversionException extends RuntimeException {

  /** serialVersionUID. */
  private static final long serialVersionUID = 7216008533037657142L;

  /**
   * Constructor without an underlying cause {@link Exception}.
   * 
   * @param message the exception message
   */
  public ConversionException(final String message) {
    super(message);
  }

  /**
   * Constructor with an underlying cause {@link Exception}.
   * 
   * @param message the contextual message
   * @param cause the underlying cause
   */
  public ConversionException(final String message, final Exception cause) {
    super(message, cause);
  }

}
