package com.company.imagebook.controllers;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

import com.company.imagebook.entities.Image;
import com.company.imagebook.entities.ImageType;
import com.company.imagebook.services.ImageCreateDTO;
import com.company.imagebook.services.ImageService;
import com.company.imagebook.validation.ContentSize;
import com.company.imagebook.validation.ContentType;
import java.io.IOException;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequestMapping(ImageController.ENDPOINT)
public class ImageController {

  static final String ENDPOINT = "/api/images";

  static final String IMAGE_PARAM = "image";

  static final String DESCRIPTION_PARAM = "description";

  @Autowired
  private ImageService service;

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
    val createDTO = ImageCreateDTO.of(
        description, ImageType.fromMediaType(contentType), contentBytes);
    return service.addImage(createDTO);
  }
}
