/*******************************************************************************
 * Copyright (c) 2016 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package com.sample.citybikesnyc;

import org.lambdamatic.internal.elasticsearch.BaseDocumentManagerImpl;
import org.lambdamatic.internal.elasticsearch.clientdsl.Client;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Manager for the {@link BikeStation} documents stored in Elasticsearch.
 */
public class BikeStations extends BaseDocumentManagerImpl<BikeStation, QBikeStation> {

  public BikeStations(final Client client) {
    super(client, BikeStation.class);
  }
  
}
