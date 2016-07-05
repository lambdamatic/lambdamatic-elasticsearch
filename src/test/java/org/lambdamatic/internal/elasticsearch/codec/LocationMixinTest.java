/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.internal.elasticsearch.codec;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.lambdamatic.elasticsearch.types.Location;
import org.lambdamatic.elasticsearch.types.LocationMixin;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Testing the {@link GeopointDeserializer}.
 */
public class LocationMixinTest {


  private ObjectMapper mapper;

  @Before
  public void setupMapper() {
    this.mapper = new ObjectMapper();
    mapper.addMixIn(Location.class, LocationMixin.class);
  }

  @Test
  public void shouldDeserializeLocation() throws JsonParseException, JsonMappingException, IOException {
    // given
    final InputStream content =
        LocationMixinTest.class.getClassLoader().getResourceAsStream("location.json");
    // when
    final Location location = mapper.readValue(content, Location.class);
    // then
    assertThat(location.getLatitude()).isEqualTo(41.12);
    assertThat(location.getLongitude()).isEqualTo(-71.34);
  }

}
