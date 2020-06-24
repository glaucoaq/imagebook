package com.company.imagebook.services.image;

import com.company.imagebook.entities.image.ImageType;
import lombok.NonNull;
import lombok.Value;

@Value(staticConstructor = "of")
public class ImageCreateRequest {

  @NonNull
  String description;

  @NonNull
  ImageType imageType;

  @NonNull
  byte[] contentBytes;
}
