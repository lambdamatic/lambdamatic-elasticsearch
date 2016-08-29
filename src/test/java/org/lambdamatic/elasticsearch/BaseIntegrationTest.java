/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.elasticsearch;

import static org.assertj.core.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.lambdamatic.internal.elasticsearch.clientdsl.Client;

/**
 * Base class for all integration test cases. Provides the underlying {@link Client} to connect to
 * an Elasticsearch cluster.
 */
public abstract class BaseIntegrationTest {

  protected static Client client() {
    final InputStream portsStream =
        Thread.currentThread().getContextClassLoader().getResourceAsStream("ports.properties");
    if (portsStream != null) {
      final Properties properties = new Properties();
      try {
        properties.load(portsStream);
      } catch (IOException e) {
        fail("Failed to load port properties from file", e);
      }
      return Client.connectTo(
          new HttpHost("localhost", Integer.parseInt(properties.getProperty("es.9200"))));
    }
    return Client.connectTo(new HttpHost("localhost", 9200));
  }

}
