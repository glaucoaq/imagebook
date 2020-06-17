package com.company.imagebook.validation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Target( { PARAMETER, FIELD })
@Retention(RUNTIME)
@Constraint(validatedBy = ContentSizeValidatorForMultipartFile.class)
@Documented
public @interface ContentSize {

  long min() default 1;

  long max() default Long.MAX_VALUE;

  String message() default ContentSizeValidatorForMultipartFile.VALIDATION_MESSAGE;

  Class<?>[] groups() default { };

  Class<? extends Payload>[] payload() default { };
}
