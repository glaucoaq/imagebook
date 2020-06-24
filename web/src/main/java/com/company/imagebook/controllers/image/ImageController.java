package com.company.imagebook.controllers.image;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

import com.company.imagebook.entities.image.Image;
import com.company.imagebook.entities.image.ImageType;
import com.company.imagebook.services.image.ImageCreateRequest;
import com.company.imagebook.services.image.ImageSearchRequest;
import com.company.imagebook.services.image.ImageService;
import com.company.imagebook.validation.ContentSize;
import com.company.imagebook.validation.ContentType;
import java.io.IOException;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Validated
@RestController
@RequestMapping(ImageController.ENDPOINT)
public class ImageController {

  static final String ENDPOINT = "/api/images";

  static final String IMAGE_PARAM = "image";

  static final String DESCRIPTION_PARAM = "description";

  static final String PAGE_PARAM = "page";

  static final String IMAGE_TYPE_PARAM = "image-type";

  static final String MIN_SIZE_PARAM = "min-size";

  static final String MAX_SIZE_PARAM = "max-size";

  @Autowired
  private ImageService service;

  @GetMapping
  public Iterable<Image> search(
      @RequestParam(value = DESCRIPTION_PARAM, required = false) String description,
      @RequestParam(value = IMAGE_TYPE_PARAM, required = false) ImageType imageType,
      @RequestParam(value = MIN_SIZE_PARAM, required = false) Integer minimumContentSize,
      @RequestParam(value = MAX_SIZE_PARAM, required = false) Integer maximumContentSize,
      @RequestParam(value = PAGE_PARAM, required = false) Integer pageNumber) {
    ImageSearchRequest searchRequest = ImageSearchRequest.search()
        .description(description)
        .imageType(imageType)
        .minimumContentSize(minimumContentSize)
        .maximumContentSize(maximumContentSize)
        .build();
    log.info("Search: page {}, {}", pageNumber, searchRequest);
    return service.search(searchRequest, pageNumber == null ? 0 : pageNumber);
  }

  @PostMapping(consumes = MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public Image upload(
      @RequestParam(DESCRIPTION_PARAM)
      @NotEmpty @Size(max = Image.MAX_DESCRIPTION_LENGTH)
          String description,
      @RequestPart(IMAGE_PARAM)
      @NotNull @ContentType({ "image/png", "image/jpeg" }) @ContentSize(max = Image.MAX_CONTENT_SIZE)
          MultipartFile imageFile)
      throws IOException {
    val contentBytes = imageFile.getBytes();
    val contentType = imageFile.getContentType();
    val createRequest = ImageCreateRequest.of(
        description, ImageType.fromMediaType(contentType), contentBytes);
    log.info("Image: {}({}), size: {}KB, '{}'", imageFile.getName(), contentType, imageFile.getSize(), description);
    return service.addImage(createRequest);
  }
}
