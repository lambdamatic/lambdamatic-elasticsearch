package com.sample.citybikesnyc;

import javax.annotation.Generated;

import org.lambdamatic.elasticsearch.annotations.Document;
import org.lambdamatic.elasticsearch.annotations.DocumentField;
import org.lambdamatic.elasticsearch.annotations.DocumentId;
import org.lambdamatic.elasticsearch.querydsl.types.FullTextField;
import org.lambdamatic.elasticsearch.querydsl.types.LocationField;
import org.lambdamatic.internal.elasticsearch.QueryMetadata;

/**
 * The {@link QueryMetadata} class associated with the {@link BikeStation} domain class 
 * annotated with {@link Document} or {@link EmbeddedDocument}.
 *
 * <p><strong>Note:</strong> This class is not meant to be extended by the user.</p>
 */
@Generated(value = "org.lambdamatic.lucene.apt.DocumentAnnotationProcessor")
public abstract class QBikeStation implements QueryMetadata<BikeStation> {

    /**
     * the query field for the 'id' domain class field.
     */
    @DocumentId
    public FullTextField id;

    /**
     * the query field for the 'name' domain class field.
     */
    @DocumentField(name="name")
    public FullTextField name;

    /**
     * the query field for the 'availableDocks' domain class field.
     */
    @DocumentField
    public FullTextField availableDocks;

    /**
     * the query field for the 'totalDocks' domain class field.
     */
    @DocumentField
    public FullTextField totalDocks;

    /**
     * the query field for the 'availableBikes' domain class field.
     */
    @DocumentField
    public FullTextField availableBikes;

    /**
     * the query field for the 'location' domain class field.
     */
    @DocumentField
    public LocationField location;

    /**
     * the query field for the 'status' domain class field.
     */
    @DocumentField
    public FullTextField status;

    /**
     * the query field for the 'testStation' domain class field.
     */
    @DocumentField
    public FullTextField testStation;

    /**
     * the query field for the 'executionTime' domain class field.
     */
    @DocumentField
    public FullTextField executionTime;

    
}
