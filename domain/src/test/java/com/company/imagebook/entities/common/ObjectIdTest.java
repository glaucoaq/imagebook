package com.company.imagebook.entities.common;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasLength;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URLEncoder;
import lombok.val;
import org.junit.jupiter.api.Test;

class ObjectIdTest {

  @Test
  void randomObjectIdShouldProduceFixedLengthAsciiUrlSafeString() {
    // Act
    val actual = ObjectId.randomObjectId();
    // Assert
    assertAll(
        () -> assertThat(actual, hasLength(ObjectId.ID_LENGTH)),
        () -> assertTrue(US_ASCII.newEncoder().canEncode(actual)),
        () -> assertThat(URLEncoder.encode(actual, "US-ASCII"), is(actual))
    );
  }
}
