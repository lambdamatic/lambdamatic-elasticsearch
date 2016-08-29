/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.internal.elasticsearch.searchdsl;

import java.util.Arrays;
import java.util.List;

/**
 * A boolean query.
 * 
 * @see <a href=
 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/query-dsl-bool-query.html">Bool
 *      Query documentation</a>
 */
public class BooleanQuery implements Query {

  public enum BooleanQueryType {
    AND,
    OR,
  }

  private final BooleanQueryType type;

  private final List<Query> queries;

  public static BooleanQuery and(final List<Query> queries) {
    return new BooleanQuery(BooleanQueryType.AND, queries);
  }

  public static BooleanQuery and(final Query... queries) {
    return new BooleanQuery(BooleanQueryType.AND, Arrays.asList(queries));
  }
  
  public static BooleanQuery or(final List<Query> queries) {
    return new BooleanQuery(BooleanQueryType.OR, queries);
  }
  
  public static BooleanQuery or(final Query... queries) {
    return new BooleanQuery(BooleanQueryType.OR, Arrays.asList(queries));
  }

  private BooleanQuery(final BooleanQueryType type, final List<Query> queries) {
    this.type = type;
    this.queries = queries;
  }

  public List<Query> getQueries() {
    return this.queries;
  }

  public BooleanQueryType getType() {
    return this.type;
  }

  @Override
  public Query boost(float boostFactor) {
    return this;
  }

  @Override
  public String toString() {
    return "BooleanQuery [type=" + type + ", queries=" + queries + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((queries == null) ? 0 : queries.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    BooleanQuery other = (BooleanQuery) obj;
    if (queries == null) {
      if (other.queries != null) {
        return false;
      }
    } else if (!queries.equals(other.queries)) {
      return false;
    }
    if (type != other.type) {
      return false;
    }
    return true;
  }



}
