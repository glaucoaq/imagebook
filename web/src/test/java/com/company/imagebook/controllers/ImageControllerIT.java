package com.company.imagebook.controllers;

import static com.company.imagebook.controllers.S3MockConfig.S3MOCK_IMAGE;
import static com.company.imagebook.controllers.S3MockConfig.S3MOCK_PORT;
import static com.company.imagebook.entities.Image.MAX_DESCRIPTION_LENGTH;
import static com.company.imagebook.services.amazonaws.AmazonS3Config.BUCKET_NAME;
import static java.util.Collections.nCopies;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.internal.util.StringUtil.join;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.company.imagebook.controllers.ImageControllerIT.TestInitializer;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ContextConfiguration(initializers = TestInitializer.class)
class ImageControllerIT {

  private static final String PNG_IMAGE_NAME = "blank.png";

  private static final String JPEG_IMAGE_NAME = "blank.jpeg";

  private static final String TOO_LARGE_IMAGE_NAME = "large.jpeg";

  private static final String EMPTY_IMAGE_NAME = "empty.png";

  private static final String NON_IMAGE_NAME = "image.txt";

  private static final String VALID_DESCRIPTION = "Image description";

  private static final String TOO_LONG_DESCRIPTION = join("", nCopies(MAX_DESCRIPTION_LENGTH + 1, "A"));

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
