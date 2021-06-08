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
@Constraint(validatedBy = ContentTypeValidatorForMultipartFile.class)
@Documented
public @interface ContentType {

  String[] value() default {};

  String message() default ContentTypeValidatorForMultipartFile.VALIDATION_MESSAGE;

  Class<?>[] groups() default { };

  Class<? extends Payload>[] payload() default { };
}
