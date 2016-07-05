/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.elasticsearch.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Defines a {@code location} type to map with an Elasticsearch {@code Geo_point}. The annotated
 * Java type must have two fields, one annotated with {@link Longitute} and the other one
 * {@link Latitude} to be a valid {@code Geo_point}.
 * 
 * @see Latitude
 * @see Longitude
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface GeoPoint {

}
