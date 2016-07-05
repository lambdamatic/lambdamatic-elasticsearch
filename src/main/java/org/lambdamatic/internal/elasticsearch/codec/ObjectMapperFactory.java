/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.internal.elasticsearch.codec;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * A factory for the {@link ObjectMapper}.
 */
public class ObjectMapperFactory {

  /** The usual Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(ObjectMapperFactory.class);

  private static final ObjectMapperFactory instance = new ObjectMapperFactory();

  private ObjectMapper objectMapper;

  /**
   * Initializes an {@link ObjectMapper} configured with mixins to support serialization and
   * deserialization of all built-in and user-defined domain types.
   * <p>
   * <strong>Note:</strong>The {@link ObjectMapper} is instantiated and initialized once and then
   * kept in cache, so multiple calls will retrieve the same instance.
   * </p>
   * 
   * @return the {@link ObjectMapper}
   * 
   */
  public static ObjectMapper getObjectMapper() {
    if (instance.objectMapper == null) {
      LOGGER.info("Initializing the ObjectMapper");
      final ObjectMapper mapper = new ObjectMapper();
      final ExecutorService availableProcessorsThreadPool =
          Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
      final Reflections reflections = new Reflections(new ConfigurationBuilder()

          // TODO: allow for configuration settings to reduce the scope of searching, using package
          // names instead of a classloader
          .setUrls(ClasspathHelper.forPackage(""))
          // .setUrls(ClasspathHelper.forClassLoader())
          .setScanners(new SubTypesScanner(), new TypeAnnotationsScanner())
          .setExecutorService(availableProcessorsThreadPool));
      // thread pool must be closed after it has been used, to avoid leaking threads in the JVM.
      availableProcessorsThreadPool.shutdown();
      // final Reflections reflections = new Reflections();
      reflections.getTypesAnnotatedWith(Mixin.class).stream().forEach(mixin -> {
        final Mixin mixinAnnotation = mixin.getAnnotation(Mixin.class);
        LOGGER.info("Adding mixin {} to {}", mixin, mixinAnnotation.target());
        mapper.addMixIn(mixinAnnotation.target(), mixin);
      });
      mapper.registerModule(new JavaTimeModule());
      // configure LocalDate serialization as string with pattern: YYYY-mm-dd
      mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
      instance.objectMapper = mapper;
    }
    return instance.objectMapper;
  }

}
