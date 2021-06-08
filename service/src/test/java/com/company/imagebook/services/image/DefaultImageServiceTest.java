package com.company.imagebook.services.image;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.company.imagebook.entities.image.Image;
import com.company.imagebook.entities.image.ImageRepository;
import com.company.imagebook.entities.image.ImageType;
import com.company.imagebook.services.exceptions.ConflictException;
import com.company.imagebook.services.storage.StorageService;
import java.net.URL;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

@ExtendWith(MockitoExtension.class)
class DefaultImageServiceTest {

  @Mock
  StorageService storageService;

  @Mock
  ImageRepository repository;

  @InjectMocks
  DefaultImageService service;

  @Test
  void givenValidImageDataWhenAddImageThenStoreContentsAndSaveMetadataToRepository() throws Exception {
    // Arrange
    val description = "Any";
    val imageType = ImageType.JPG;
    val contentBytes = new byte[] { 0 };
    val createDTO = ImageCreateRequest.of(description, imageType, contentBytes);
    val imageURL = new URL("http://example/image");

    when(storageService.putObject(anyString(), eq(contentBytes))).thenReturn(imageURL);
    when(repository.save(any(Image.class))).then(invocation -> invocation.getArgument(0));

    // Act
    val actual = service.addImage(createDTO);

    // Assert
    assertAll(
        () -> assertThat(actual.getDescription(), is(description)),
        () -> assertThat(actual.getImageUrl(), is(imageURL)),
        () -> assertThat(actual.getContentSize(), is(1)),
        () -> assertThat(actual.getImageType(), is(imageType))
    );
  }

  @Test
  void givenRepositoryInErrorWhenAddImageThenRemoveObjectAndThrowConflictException() throws Exception {
    // Arrange
    val createDTO = ImageCreateRequest.of("any", ImageType.JPG, new byte[0]);
    val imageURL = new URL("http://example/image");

    when(storageService.putObject(anyString(), any(byte[].class))).thenReturn(imageURL);
    doThrow(new DataAccessException("any") {}).when(repository).save(any(Image.class));

    // Act & Assert
    assertThrows(ConflictException.class, () -> service.addImage(createDTO));
    verify(storageService).removeObject(anyString());
  }
}
