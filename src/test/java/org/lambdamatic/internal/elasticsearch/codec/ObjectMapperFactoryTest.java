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

package org.lambdamatic.internal.elasticsearch.codec;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.lambdamatic.elasticsearch.types.Location;
import org.lambdamatic.elasticsearch.types.LocationMixin;
import org.lambdamatic.internal.elasticsearch.codec.ObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Testing the {@link ObjectMapperFactory}.
 */
public class ObjectMapperFactoryTest {

  @Test
  public void shouldInitializeObjectMapperWithMixins() {
    
    // when
    final ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
    // then
    assertThat(objectMapper.findMixInClassFor(Location.class)).isEqualTo(LocationMixin.class);
  }
}
