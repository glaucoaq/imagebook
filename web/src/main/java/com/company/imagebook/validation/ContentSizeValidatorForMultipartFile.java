package com.company.imagebook.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

public class ContentSizeValidatorForMultipartFile implements ConstraintValidator<ContentSize, MultipartFile> {

  public static final String VALIDATION_MESSAGE = "Content size is not within the specified range.";

  private long minLength;

  private long maxLength;

  @Override
  public void initialize(ContentSize parameters) {
    minLength = parameters.min();
    maxLength = parameters.max();
  }

  @Override
  public boolean isValid(MultipartFile value, ConstraintValidatorContext context) {
    return value.getSize() >= minLength && value.getSize() <= maxLength;
  }
}
