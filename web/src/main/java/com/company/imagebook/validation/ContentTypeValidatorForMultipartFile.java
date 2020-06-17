package com.company.imagebook.validation;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;

import java.util.HashSet;
import java.util.Set;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

class ContentTypeValidatorForMultipartFile implements ConstraintValidator<ContentType, MultipartFile> {

  static final String VALIDATION_MESSAGE = "The content type of the multipart file is not supported.";

  private Set<String> validContentTypes;

  @Override
  public void initialize(ContentType parameters) {
    validContentTypes = unmodifiableSet(new HashSet<>(asList(parameters.value())));
  }

  @Override
  public boolean isValid(MultipartFile value, ConstraintValidatorContext context) {
    return validContentTypes.contains(value.getContentType());
  }
}
