/*******************************************************************************
 * Copyright (c) 2014, 2015 Red Hat, Inc. Distributed under license by Red Hat, Inc. All rights
 * reserved. This program is made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/

package org.lambdamatic.elasticsearch.querydsl.types;

import org.lambdamatic.elasticsearch.types.Location;
import org.lambdamatic.internal.elasticsearch.QueryClauseType;
import org.lambdamatic.internal.elasticsearch.QueryClauseType.EnumQueryClauseType;

/**
 * API for geo-search operations related to {@link Location}.
 * 
 * @author Xavier Coulon
 *
 */
public interface LocationField {

  /**
   * Finds documents with geo-points that fall into the specified rectangle.
   * 
   * @param topLeft the top left corner of the rectangle
   * @param bottomRight the bottom right corner of the rectangle
   * 
   * @see <a href=
   *      "https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-geo-bounding-box-query.html">Geo
   *      bounding box queries</a>
   */
  //TODO: return Boostable
  @QueryClauseType(EnumQueryClauseType.GEO_WITHIN_RECTANGLE)
  public void withinRectangle(final Location topLeft, final Location bottomRight);

}
