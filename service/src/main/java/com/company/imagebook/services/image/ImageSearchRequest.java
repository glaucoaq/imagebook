package com.company.imagebook.services.image;

import com.company.imagebook.entities.image.ImageType;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(builderMethodName = "search")
public class ImageSearchRequest {

  String description;

  ImageType imageType;

  Integer minimumContentSize;

  Integer maximumContentSize;
}
