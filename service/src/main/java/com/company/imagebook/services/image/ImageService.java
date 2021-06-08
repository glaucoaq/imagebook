package com.company.imagebook.services.image;

import com.company.imagebook.entities.image.Image;

public interface ImageService {

  Iterable<Image> search(ImageSearchRequest request, int page);

  Image addImage(ImageCreateRequest request);
}
