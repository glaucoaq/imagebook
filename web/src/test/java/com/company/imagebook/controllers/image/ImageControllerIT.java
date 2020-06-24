package com.company.imagebook.controllers.image;

import static com.company.imagebook.controllers.image.S3MockConfig.S3MOCK_IMAGE;
import static com.company.imagebook.controllers.image.S3MockConfig.S3MOCK_PORT;
import static com.company.imagebook.entities.image.Image.MAX_DESCRIPTION_LENGTH;
import static com.company.imagebook.services.storage.amazonaws.AmazonS3Config.BUCKET_NAME;
import static java.util.Arrays.asList;
import static java.util.Collections.nCopies;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.internal.util.StringUtil.join;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.company.imagebook.controllers.image.ImageControllerIT.TestInitializer;
import com.company.imagebook.entities.image.ImageType;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.http.client.utils.URIBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(properties = {
    "aws.s3.service-endpoint=http://localhost:9090/",
    "imagebook.search-size=2"
})
@Testcontainers
@AutoConfigureMockMvc
@ContextConfiguration(initializers = TestInitializer.class)
class ImageControllerIT {

  private static final String PNG_IMAGE_NAME = "blank.png";

  private static final String JPEG_IMAGE_NAME = "blank.jpeg";

  private static final String TOO_LARGE_IMAGE_NAME = "large.png";

  private static final String EMPTY_IMAGE_NAME = "empty.png";

  private static final String NON_IMAGE_NAME = "image.txt";

  private static final String VALID_DESCRIPTION = "Image description";

  private static final String TOO_LONG_DESCRIPTION = join("", nCopies(MAX_DESCRIPTION_LENGTH + 1, "A"));

  private static final int FIRST_PAGE = 0;

  private static final String SEARCH_ALL_DESCRIPTION = "all";

  private static final String SINGLE_ITEM_DESCRIPTION = "odd";

  private static final int SINGLE_ITEM_SIZE = 100;

  private static final String NO_ITEM_DESCRIPTION = "al";

  @Container
  public static final GenericContainer<?> S3_MOCK_CONTAINER = new GenericContainer<>(S3MOCK_IMAGE)
      .withExposedPorts(S3MOCK_PORT);

  @Container
  public static final MySQLContainer<?> MYSQL_CONTAINER = new MySQLContainer<>();

  static class TestInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    @SneakyThrows
    public void initialize(ConfigurableApplicationContext applicationContext) {
      TestPropertyValues.of(
          "spring.datasource.url=" + MYSQL_CONTAINER.getJdbcUrl(),
          "spring.datasource.username=" + MYSQL_CONTAINER.getUsername(),
          "spring.datasource.password=" + MYSQL_CONTAINER.getPassword(),
          "aws.s3.service-endpoint=" + createS3Endpoint().toString(),
          "aws.s3.region=us-east-2",
          "aws.s3.access-key=none",
          "aws.s3.secret-key=none"
      ).applyTo(applicationContext.getEnvironment());
    }
  }

  @Autowired
  private MockMvc mockMvc;

  @ParameterizedTest
  @ValueSource(strings = {PNG_IMAGE_NAME, JPEG_IMAGE_NAME})
  void postImageShouldReturnCreatedStatusAndLocationForImage(final String imageName) throws Exception {
    val imageFile = createMultipartFile(imageName);
    val imageSize = imageFile.getSize();
    val imageUrlPrefix = new URIBuilder(createS3Endpoint()).setPath(BUCKET_NAME).build().toString();
    mockMvc.perform(multipart(ImageController.ENDPOINT)
        .file(imageFile)
        .param(ImageController.DESCRIPTION_PARAM, VALID_DESCRIPTION))
        .andDo(log())
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").isNotEmpty())
        .andExpect(jsonPath("$.description", is(VALID_DESCRIPTION)))
        .andExpect(jsonPath("$.imageUrl", startsWith(imageUrlPrefix)))
        .andExpect(jsonPath("$.contentSize", is((int) imageSize)));
  }

  private static Stream<Arguments> requestErrorsFixture() {
    return Stream.of(
        Arguments.of(EMPTY_IMAGE_NAME, VALID_DESCRIPTION, BAD_REQUEST),
        Arguments.of(TOO_LARGE_IMAGE_NAME, VALID_DESCRIPTION, BAD_REQUEST),
        Arguments.of(NON_IMAGE_NAME, VALID_DESCRIPTION, BAD_REQUEST),
        Arguments.of(PNG_IMAGE_NAME, TOO_LONG_DESCRIPTION, BAD_REQUEST),
        Arguments.of(PNG_IMAGE_NAME, "", BAD_REQUEST)
    );
  }

  @ParameterizedTest
  @MethodSource("requestErrorsFixture")
  void postImageShouldFailWithStatusForImageAndDescription(final String imageName, final String description,
      final HttpStatus status) throws Exception {
    mockMvc.perform(multipart(ImageController.ENDPOINT)
        .file(createMultipartFile(imageName))
        .param(ImageController.DESCRIPTION_PARAM, description))
        .andDo(log())
        .andExpect(status().is(status.value()));
  }

  @Test
  void postImageShouldFailWithStatusForMissingDescription() throws Exception {
    mockMvc.perform(multipart(ImageController.ENDPOINT)
        .file(createMultipartFile(PNG_IMAGE_NAME)))
        .andDo(log())
        .andExpect(status().isBadRequest());
  }

  @Test
  void postImageShouldFailWithBadRequestForMissingImage() throws Exception {
    mockMvc.perform(multipart(ImageController.ENDPOINT)
        .param(ImageController.DESCRIPTION_PARAM, VALID_DESCRIPTION))
        .andDo(log())
        .andExpect(status().isBadRequest());
  }

  private static Stream<Arguments> searchImagesFixture() {
    return Stream.of(
        Arguments.of(null, null, null, null, FIRST_PAGE, asList("key0", "key1")),
        Arguments.of(SEARCH_ALL_DESCRIPTION, null, null, null, FIRST_PAGE, asList("key0", "key1")),
        Arguments.of(SEARCH_ALL_DESCRIPTION, null, null, null, FIRST_PAGE + 1, singletonList("key2")),
        Arguments.of(SINGLE_ITEM_DESCRIPTION, null, null, null, FIRST_PAGE, singletonList("key1")),
        Arguments.of(SEARCH_ALL_DESCRIPTION, ImageType.JPG, SINGLE_ITEM_SIZE - 1, SINGLE_ITEM_SIZE + 1,
            FIRST_PAGE, singletonList("key1")));
  }

  @ParameterizedTest
  @Sql("/sql/insert-images.sql")
  @MethodSource("searchImagesFixture")
  void searchImageShouldReturnExpectedItems(String description, ImageType imageType, Integer minimumContentSize,
      Integer maximumContentSize, Integer pageNumber, List<String> expectedKeys) throws Exception {
    val request = get(ImageController.ENDPOINT)
        .param(ImageController.DESCRIPTION_PARAM, description)
        .param(ImageController.PAGE_PARAM, pageNumber == null ? "0" : pageNumber.toString());
    if (imageType != null) {
      request.param(ImageController.IMAGE_TYPE_PARAM, imageType.toString());
    }
    if (minimumContentSize != null) {
      request.param(ImageController.MIN_SIZE_PARAM, minimumContentSize.toString());
    }
    if (maximumContentSize != null) {
      request.param(ImageController.MAX_SIZE_PARAM, maximumContentSize.toString());
    }
    val expectedIds = expectedKeys.toArray(new String[0]);

    mockMvc.perform(request)
        .andDo(log())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content", hasSize(expectedKeys.size())))
        .andExpect(jsonPath("$.content[*].id", containsInAnyOrder(expectedIds)));
  }

  @Test
  @Sql("/sql/insert-images.sql")
  void searchImageShouldReturnEmptyResultForNonExistingTerm() throws Exception {
    mockMvc.perform(get(ImageController.ENDPOINT)
        .param(ImageController.DESCRIPTION_PARAM, NO_ITEM_DESCRIPTION))
        .andDo(log())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content").isEmpty());
  }

  @Test
  void searchImageShouldFailWithStatusForUnrecognizedType() throws Exception {
    mockMvc.perform(get(ImageController.ENDPOINT)
        .param(ImageController.IMAGE_TYPE_PARAM, "WRONG"))
        .andDo(log())
        .andExpect(status().isBadRequest());
  }

  private static URI createS3Endpoint() throws URISyntaxException {
    return new URIBuilder()
        .setScheme("http")
        .setHost(S3_MOCK_CONTAINER.getHost())
        .setPort(S3_MOCK_CONTAINER.getFirstMappedPort())
        .build();
  }

  private static MockMultipartFile createMultipartFile(String name) throws IOException {
    val imageLocation = "/image/" + name;
    val imageType = name.split("\\.")[1];
    val inputStream = ImageControllerIT.class.getResourceAsStream(imageLocation);
    return new MockMultipartFile(ImageController.IMAGE_PARAM, imageLocation, "image/" + imageType, inputStream);
  }
}
