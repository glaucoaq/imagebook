package com.company.imagebook.services.storage.amazonaws;

import static com.company.imagebook.services.storage.amazonaws.AmazonS3Config.BUCKET_NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amazonaws.services.s3.AmazonS3;
import java.net.MalformedURLException;
import java.net.URL;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AmazonS3StorageServiceTest {

  @Mock
  AmazonS3 amazonS3;

  @InjectMocks
  AmazonS3StorageService service;

  @Test
  void giveKeyAndBytesWhenPutObjectThenReturnAmazonS3ClientObjectUrl() throws MalformedURLException {
    // Arrange
    val key = "ANY";
    val bytes = new byte[0];
    val expected = new URL("http://example/key");
    when(amazonS3.getUrl(eq(BUCKET_NAME), eq(key))).thenReturn(expected);
    // Act
    val actual = service.putObject(key, bytes);
    // Assert
    verify(amazonS3).putObject(eq(BUCKET_NAME), eq(key), notNull(), notNull());
    assertThat(actual, is(expected));
  }

  @Test
  void givenKeyWhenRemoveObjectThenCallAmazonS3ClientRemove() {
    // Arrange
    val key = "ANY";
    // Act
    service.removeObject(key);
    // Assert
    verify(amazonS3).deleteObject(eq(BUCKET_NAME), eq(key));
  }
}
