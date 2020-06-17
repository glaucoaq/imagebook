package com.company.imagebook.entities;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ImageType {
  PNG("image/png"),
  JPG("image/jpeg");

  private final String mediaType;

  public static ImageType fromMediaType(@NonNull String mediaType) {
    for (ImageType type : ImageType.values()) {
      if (mediaType.equals(type.mediaType)) {
        return type;
      }
    }
    throw new IllegalArgumentException(mediaType + " is not a supported ImageType value.");
  }
}
