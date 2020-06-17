package com.company.imagebook.services;

import com.company.imagebook.entities.Image;

public interface ImageService {

  Image addImage(ImageCreateDTO createDTO);
}
