package com.company.imagebook.services.image;

import static java.lang.Math.max;

import com.company.imagebook.entities.common.ObjectId;
import com.company.imagebook.entities.image.Image;
import com.company.imagebook.entities.image.ImageRepository;
import com.company.imagebook.services.exceptions.ConflictException;
import com.company.imagebook.services.storage.StorageService;
import java.net.URL;
import java.util.function.BiFunction;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class DefaultImageService implements ImageService {

  @Autowired
  private StorageService storageService;

  @Autowired
  private ImageRepository repository;

  @Override
  public Image addImage(@NonNull final ImageCreateDTO createDTO) {
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
