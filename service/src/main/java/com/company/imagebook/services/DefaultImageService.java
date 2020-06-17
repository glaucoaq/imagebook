package com.company.imagebook.services;

import com.company.imagebook.common.ObjectId;
import com.company.imagebook.entities.Image;
import com.company.imagebook.exceptions.ConflictException;
import com.company.imagebook.repositories.ImageRepository;
import java.net.URL;
import java.util.function.BiFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
public class DefaultImageService implements ImageService {

  @Autowired
  private StorageService storageService;

  @Autowired
  private ImageRepository repository;

  @Override
  public Image addImage(ImageCreateDTO createDTO) {
    String key = ObjectId.randomObjectId();
    URL imageUrl = storageService.putObject(key, createDTO.getContentBytes());
    try {
      return repository.save(createDTO.asImage(key, imageUrl));
    } catch (Exception e) {
      storageService.removeObject(key);
      BiFunction<String, Throwable, ? extends RuntimeException> rethrow =
          e instanceof DataAccessException ? ConflictException::new : RuntimeException::new;
      throw rethrow.apply("Error while saving the image metadata.", e);
    }
  }
}
