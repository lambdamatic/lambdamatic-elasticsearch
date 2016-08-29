package com.sample.citybikesnyc;

import javax.annotation.Generated;

import org.lambdamatic.elasticsearch.annotations.Document;
import org.lambdamatic.elasticsearch.annotations.DocumentField;
import org.lambdamatic.elasticsearch.annotations.DocumentIdField;
import org.lambdamatic.elasticsearch.annotations.Embedded;
import org.lambdamatic.elasticsearch.annotations.FullText;
import org.lambdamatic.elasticsearch.annotations.Keyword;
import org.lambdamatic.elasticsearch.searchdsl.types.FullTextField;
import org.lambdamatic.elasticsearch.searchdsl.types.KeywordField;
import org.lambdamatic.elasticsearch.searchdsl.types.Location;
import org.lambdamatic.internal.elasticsearch.QueryMetadata;

/**
 * The {@link QueryMetadata} class associated with the {@link BikeStation} domain class annotated
 * with {@link Document} or {@link EmbeddedDocument}.
 *
 * <p>
 * <strong>Note:</strong> This class is not meant to be extended by the user.
 * </p>
 */
@Generated(value = "org.lambdamatic.lucene.apt.DocumentAnnotationProcessor")
public abstract class QBikeStation implements QueryMetadata<BikeStation> {

  /**
   * the query field for the 'name' domain class field.
   */
  @DocumentField(name = "name")
  @FullText
  public FullText name;

  /**
   * the query field for the 'availableDocks' domain class field.
   */
  @DocumentField(name = "availableDocks")
  @Keyword
  public Keyword availableDocks;

  /**
   * the query field for the 'totalDocks' domain class field.
   */
  @DocumentField(name = "totalDocks")
  @Keyword
  public Keyword totalDocks;

  /**
   * the query field for the 'availableBikes' domain class field.
   */
  @DocumentField(name = "availableBikes")
  @Keyword
  public Keyword availableBikes;

  /**
   * the query field for the 'location' domain class field.
   */
  @DocumentField(name = "location")
  public Location location;

  /**
   * the query field for the 'status' domain class field.
   */
  @DocumentField(name = "status")
  @Keyword
  public Keyword status;

  /**
   * the query field for the 'testStation' domain class field.
   */
  @DocumentField(name = "testStation")
  @Keyword
  public FullText testStation;

  /**
   * the query field for the 'executionTime' domain class field.
   */
  @DocumentField(name = "executionTime")
  @Keyword
  public FullText executionTime;

}
