package com.company.imagebook.services;

import com.company.imagebook.entities.Image;
import com.company.imagebook.entities.ImageType;
import java.net.URL;
import lombok.NonNull;
import lombok.Value;

@Value(staticConstructor = "of")
public class ImageCreateDTO {

  @NonNull
  String description;

  @NonNull
  ImageType imageType;

  @NonNull
  byte[] contentBytes;

  Image asImage(@NonNull final String id, @NonNull final URL contentUrl) {
    return new Image()
        .setId(id)
        .setDescription(description)
        .setContentSize(contentBytes.length)
        .setImageUrl(contentUrl)
        .setImageType(imageType);
  }
}
