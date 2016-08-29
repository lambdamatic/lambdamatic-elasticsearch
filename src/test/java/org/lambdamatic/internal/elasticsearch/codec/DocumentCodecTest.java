/*******************************************************************************
 * Copyright (c) 2016 Red Hat. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Red Hat - Initial Contribution
 *******************************************************************************/

package org.lambdamatic.internal.elasticsearch.codec;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.function.Supplier;

import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import org.json.JSONException;
import org.junit.Ignore;
import org.junit.Test;
import org.lambdamatic.elasticsearch.types.Location;
import org.lambdamatic.internal.elasticsearch.MappingException;
import org.lambdamatic.internal.elasticsearch.codec.DocumentCodec;
import org.lambdamatic.internal.elasticsearch.codec.ObjectMapperFactory;
import org.skyscreamer.jsonassert.JSONAssert;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sample.blog.Blogpost;
import com.sample.blog.Comment;
import com.sample.citybikesnyc.BikeStation;
import com.sample.citybikesnyc.BikeStationDeserializer;
import com.sample.citybikesnyc.BikeStationStatus;

/**
 * Testing the {@link DocumentCodec} class.
 */
public class DocumentCodecTest {

  @Test
  public void shouldFindIdField() {
    // when
    final String idFieldName = DocumentCodec.getIdField(Blogpost.class).getName();
    // then
    Assertions.assertThat(idFieldName).isEqualTo("id");
  }

  @Ignore
  @Test(expected = MappingException.class)
  public void shouldNotValidateWhenNoIdFieldExists() {
    // when
    DocumentCodec.getIdField(Comment.class).getName();
    // expect an exception
  }

  @Ignore
  @Test(expected = MappingException.class)
  public void shouldNotValidateWhenTooManyIdFieldsExist() {
    fail("Not implemented yet");
  }

  @Ignore
  @Test(expected = MappingException.class)
  public void shouldValidateCustomLocationType() {
    fail("Not implemented yet");
  }

  @Ignore
  @Test(expected = MappingException.class)
  public void shouldNotValidateCustomLocationTypeMissingLatitudeField() {
    fail("Not implemented yet");
  }

  @Ignore
  @Test(expected = MappingException.class)
  public void shouldNotValidateWhenCustomLocationTypeMissingLongitudeField() {
    fail("Not implemented yet");
  }

  @Test
  public void shouldEncodeBlogpost() throws IOException, JSONException {
    // given
    final Blogpost blogpost = new Blogpost();
    blogpost.setId(1L);
    blogpost.setTitle("blog post");
    blogpost.setContent("Lorem ipsum...");
    blogpost.setTags(new String[] {"foo", "bar"});
    final Comment firstComment = new Comment("Xavier", "Nice work!", 5, LocalDate.of(2016, 4, 1));
    final Comment secondComment =
        new Comment("Xavier", "this looks good", 5, LocalDate.of(2016, 7, 3));
    blogpost.setComments(Arrays.asList(firstComment, secondComment));
    // when
    final String actualContent =
        new DocumentCodec<>(Blogpost.class, ObjectMapperFactory.getObjectMapper()).encode(blogpost);
    // then id should not be part of the documentSource, but other fields, yes.
    final String expectedContent = IOUtils
        .toString(BikeStationDeserializer.class.getClassLoader().getResource("blogpost.json"));
    JSONAssert.assertEquals(expectedContent, actualContent, false);
  }

  @Test
  public void shouldDecodeBlogPost() throws JsonParseException, JsonMappingException, IOException {
    // given
    final InputStream content =
        BikeStationDeserializer.class.getClassLoader().getResourceAsStream("blogpost.json");
    // when
    final Blogpost blogpost =
        ObjectMapperFactory.getObjectMapper().readValue(content, Blogpost.class);
    // then
    assertThat(blogpost.getId()).isNull();
    assertThat(blogpost.getTitle()).isEqualTo("blog post");
    assertThat(blogpost.getContent()).isEqualTo("Lorem ipsum...");
    assertThat(blogpost.getTags()).containsExactly("foo", "bar");
    assertThat(blogpost.getComments()).containsExactly(
        new Comment("Xavier", "Nice work!", 5, LocalDate.of(2016, 04, 01)),
        new Comment("Xavier", "this looks good", 5, LocalDate.of(2016, 07, 03)));
  }

  @Test
  public void shouldEncodeBikeStation() throws IOException, JSONException {
    // given
    final BikeStation bikeStation = new BikeStation();
    bikeStation.setId("1");
    bikeStation.setStationName("Station 1");
    bikeStation.setAvailableBikes(5);
    bikeStation.setAvailableDocks(15);
    bikeStation.setTotalDocks(20);
    bikeStation.setStatus(BikeStationStatus.IN_SERVICE);
    bikeStation.setLocation(new Location(41.12, -71.34));
    // when
    final String actualContent =
        new DocumentCodec<>(BikeStation.class, ObjectMapperFactory.getObjectMapper())
            .encode(bikeStation);
    // then id should not be part of the documentSource, but other fields, yes.
    final String expectedContent = IOUtils
        .toString(BikeStationDeserializer.class.getClassLoader().getResource("bikestation.json"));
    JSONAssert.assertEquals(expectedContent, actualContent, false);
  }

  @Test
  public void shouldDecodeBikeStation()
      throws JsonParseException, JsonMappingException, IOException {
    // given
    final InputStream content =
        BikeStationDeserializer.class.getClassLoader().getResourceAsStream("bikestation.json");
    // when
    final BikeStation bikeStation =
        ObjectMapperFactory.getObjectMapper().readValue(content, BikeStation.class);
    // then
    assertThat(bikeStation.getId()).isNull();
    assertThat(bikeStation.getStationName()).isEqualTo("Station 1");
    assertThat(bikeStation.getAvailableDocks()).isEqualTo(15);
    assertThat(bikeStation.getStatus()).isEqualTo(BikeStationStatus.IN_SERVICE);
    assertThat(bikeStation.getLocation()).isEqualTo(new Location(41.12, -71.34));
  }

}
