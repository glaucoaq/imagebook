package com.company.imagebook.services.image;

import static com.company.imagebook.entities.image.ImageSpecifications.descriptionContains;
import static com.company.imagebook.entities.image.ImageSpecifications.hasMaxSize;
import static com.company.imagebook.entities.image.ImageSpecifications.hasMinSize;
import static com.company.imagebook.entities.image.ImageSpecifications.hasType;
import static java.lang.Math.max;
import static org.springframework.data.jpa.domain.Specification.where;

import com.company.imagebook.entities.common.ObjectId;
import com.company.imagebook.entities.image.Image;
import com.company.imagebook.entities.image.ImageRepository;
import com.company.imagebook.services.exceptions.ConflictException;
import com.company.imagebook.services.storage.StorageService;
import java.net.URL;
import java.util.function.BiFunction;
import lombok.NonNull;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class DefaultImageService implements ImageService {

  @Autowired
  private StorageService storageService;

  @Autowired
  private ImageRepository repository;

  @Value("${imagebook.search-size}")
  private int searchResultSize;

  @Override
  public Iterable<Image> search(@NonNull final ImageSearchRequest request, final int page) {
    @SuppressWarnings("ConstantConditions")
    val spec = where(descriptionContains(request.getDescription()))
        .and(hasType(request.getImageType()))
        .and(hasMinSize(request.getMinimumContentSize()))
        .and(hasMaxSize(request.getMaximumContentSize()));
    val pageable = PageRequest.of(max(page, 0), searchResultSize);
    return repository.findAll(spec, pageable);
  }

  @Override
  public Image addImage(@NonNull final ImageCreateRequest request) {
    val key = ObjectId.randomObjectId();
    val contentBytes = request.getContentBytes();
    final URL imageUrl = storageService.putObject(key, contentBytes);
    try {
      val image = new Image()
          .setId(key)
          .setDescription(request.getDescription())
          .setContentSize(contentBytes.length)
          .setImageUrl(imageUrl)
          .setImageType(request.getImageType());
      return repository.save(image);
    } catch (Exception e) {
      storageService.removeObject(key);
      BiFunction<String, Throwable, ? extends RuntimeException> rethrow =
          e instanceof DataAccessException ? ConflictException::new : RuntimeException::new;
      throw rethrow.apply("Error while saving the image metadata.", e);
    }
  }
}
