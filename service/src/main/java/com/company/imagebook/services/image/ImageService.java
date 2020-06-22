package com.company.imagebook.services.image;

import com.company.imagebook.entities.image.Image;

public interface ImageService {

  Image addImage(ImageCreateDTO createDTO);
}
