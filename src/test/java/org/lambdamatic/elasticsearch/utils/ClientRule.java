/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.elasticsearch.utils;

import java.net.InetAddress;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.junit.Rule;
import org.junit.rules.ExternalResource;

/**
 * A Junit {@link Rule} to setup an Elasticsearch {@link Client}
 * 
 * <p>
 * Note: Client configuration is found in /src/test/resources/elasticsearch.yml
 * </p>
 */
public class ClientRule extends ExternalResource {

  private Client client;

  /**
   * @return the configured {@link Client}
   */
  public Client getClient() {
    return client;
  }

  @Override
  protected void before() throws Throwable {
    // TransportClient.builder().build()
    // .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("host1"), 9300));
    // final Settings settings = Settings.builder().EMPTY;
    // this.client = new TransportClient.Builder().settings().build();
    final Path pathToConfig = Paths.get(
        Thread.currentThread().getContextClassLoader().getResource("elasticsearch.yml").toURI());
    final Settings settings = Settings.builder().loadFromPath(pathToConfig).build();
    this.client = new TransportClient.Builder().settings(settings).build().addTransportAddress(
        new InetSocketTransportAddress(InetAddress.getByName("192.168.99.101"), 9301));
  }

  @Override
  protected void after() {
    if (client != null) {
      client.close();
    }
  }

}
