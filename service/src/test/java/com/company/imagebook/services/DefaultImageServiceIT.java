package com.company.imagebook.services;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.util.IOUtils;
import com.company.imagebook.IntegrationTestConfig;
import com.company.imagebook.entities.ImageType;
import com.company.imagebook.services.amazonaws.AmazonS3Config;
import java.time.Instant;
import javax.persistence.EntityManager;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = IntegrationTestConfig.class)
class DefaultImageServiceIT {

  private static final String DESCRIPTION = "Image";

  private static final byte[] IMAGE_BYTES = { 1, 2, 3 };

  private static final String IMAGE_ID_SQL = "select id from image where description = 'Image'";

  @Autowired
  private DefaultImageService service;

  @Autowired
  AmazonS3 s3Client;

  @Autowired
  EntityManager entityManager;

  @Test
  void addImageShouldSaveInDatabaseAndS3Bucket() throws Exception {
    // Arrange
    val createDTO = ImageCreateDTO.of(DESCRIPTION, ImageType.PNG, IMAGE_BYTES);
    val before = Instant.now();

    // Act
    val actual = service.addImage(createDTO);

    val actualId = (String) entityManager
        .createNativeQuery(IMAGE_ID_SQL)
        .getSingleResult();
    val actualBytes = IOUtils.toByteArray(s3Client
        .getObject(AmazonS3Config.BUCKET_NAME, actual.getId())
        .getObjectContent());

    // Assert
    val after = Instant.now();
    assertAll(
        () -> assertThat(actualId, notNullValue()),
        () -> assertThat(actualBytes, is(IMAGE_BYTES)),
        () -> assertThat(actual.getId(), is(actualId)),
        () -> assertThat(actual.getDescription(), is(DESCRIPTION)),
        () -> assertThat(actual.getImageType(), is(ImageType.PNG)),
        () -> assertThat(actual.getContentSize(), is(IMAGE_BYTES.length)),
        () -> assertThat(actual.getImageUrl().getPath(), is("/imagebook/" + actualId)),
        () -> assertThat(actual.getCreatedDate(), greaterThanOrEqualTo(before)),
        () -> assertThat(actual.getCreatedDate(), lessThanOrEqualTo(after)),
        () -> assertThat(actual.getLastModifiedDate(), greaterThanOrEqualTo(before)),
        () -> assertThat(actual.getLastModifiedDate(), lessThanOrEqualTo(after))
    );
  }
}
