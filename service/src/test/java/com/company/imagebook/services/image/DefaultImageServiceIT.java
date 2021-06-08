package com.company.imagebook.services.image;

import static com.company.imagebook.services.image.ImageSearchRequest.search;
import static com.google.common.collect.Iterables.transform;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.util.IOUtils;
import com.company.imagebook.IntegrationTestConfig;
import com.company.imagebook.entities.image.Image;
import com.company.imagebook.entities.image.ImageType;
import com.company.imagebook.services.storage.amazonaws.AmazonS3Config;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest(classes = IntegrationTestConfig.class)
class DefaultImageServiceIT {

  private static final String DESCRIPTION = "Image";

  private static final byte[] IMAGE_BYTES = { 1, 2, 3 };

  private static final String IMAGE_ID_SQL = "select id from image where description = 'Image'";

  private static final int FIRST_PAGE = 0;

  @Autowired
  private DefaultImageService service;

  @Autowired
  AmazonS3 s3Client;

  @Autowired
  EntityManager entityManager;

  @Test
  void addImageShouldSaveInDatabaseAndS3Bucket() throws Exception {
    // Arrange
    val createDTO = ImageCreateRequest.of(DESCRIPTION, ImageType.PNG, IMAGE_BYTES);
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

  @ParameterizedTest
  @Sql("/sql/insert-images.sql")
  @MethodSource("searchImagesFixture")
  void searchShouldFindExpectedIds(ImageSearchRequest request, int page, List<Integer> numbers) {
    // Arrange
    val expectedIds = numbers.stream().map(num -> "key" + num).toArray(String[]::new);

    // Act
    val actual = service.search(request, page);

    // Assert
    assertThat(transform(actual, Image::getId), containsInAnyOrder(expectedIds));
  }

  private static Stream<Arguments> searchImagesFixture() {
    return Stream.of(
        Arguments.of(search().description("first").build(), FIRST_PAGE, singletonList(1)),
        Arguments.of(search().description("last").build(), FIRST_PAGE, singletonList(9)),
        Arguments.of(search().description("all").build(), FIRST_PAGE, asList(1, 2, 3)),
        Arguments.of(search().description("all").build(), FIRST_PAGE + 1, asList(4, 5, 6)),
        Arguments.of(search().description("all").build(), FIRST_PAGE + 2, asList(7, 8, 9)),
        Arguments.of(search().description("small").build(), FIRST_PAGE, asList(1, 4, 7)),
        Arguments.of(search().maximumContentSize(10).build(), FIRST_PAGE, asList(1, 4, 7)),
        Arguments.of(search().description("odd").maximumContentSize(10).build(), FIRST_PAGE, asList(1, 7)),
        Arguments.of(search().description("jpg").build(), FIRST_PAGE, asList(2, 3, 6)),
        Arguments.of(search().description("jpg").build(), FIRST_PAGE + 1, singletonList(8)),
        Arguments.of(search().imageType(ImageType.JPG).build(), FIRST_PAGE, asList(2, 3, 6)),
        Arguments.of(search().imageType(ImageType.JPG).build(), FIRST_PAGE + 1, singletonList(8)),
        Arguments.of(search().description("odd").imageType(ImageType.JPG).build(), FIRST_PAGE, singletonList(3)),
        Arguments.of(
            search().description("odd").imageType(ImageType.PNG).minimumContentSize(0).maximumContentSize(10).build(),
            FIRST_PAGE, asList(1, 7)));
  }
}
