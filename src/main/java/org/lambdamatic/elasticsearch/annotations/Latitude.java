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

import org.lambdamatic.elasticsearch.types.Location;

/**
 * Defines the {@code latitude} field in an Elasticsearch {@code Geo_point}. The Java type used in
 * the domain model must have two fields, one annotated with {@link Longitude} and the other one
 * {@link Latitude} to be a valid {@code Geo_point}.
 * 
 * @see Longitude
 * @see Location
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Latitude {

}
