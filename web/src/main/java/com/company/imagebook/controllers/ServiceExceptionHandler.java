package com.company.imagebook.controllers;

import static java.util.stream.Collectors.joining;

import com.company.imagebook.services.exceptions.ConflictException;
import com.company.imagebook.services.exceptions.MalformedSearchSyntaxException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@ControllerAdvice
public class ServiceExceptionHandler extends ResponseEntityExceptionHandler {

  @Value(staticConstructor = "of")
  public static class ErrorRecord {

    String category;

    String message;
  }

  @ExceptionHandler(ConflictException.class)
  protected ResponseEntity<ErrorRecord> handleConflict(ConflictException exception) {
    log.info("Cannot handle request due to conflict error: {}", exception.getMessage());
    return ResponseEntity
        .status(HttpStatus.CONFLICT)
        .body(ErrorRecord.of(ConflictException.class.getSimpleName(), exception.getMessage()));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  protected ResponseEntity<ErrorRecord> handleValidationException(ConstraintViolationException exception) {
    log.info("Cannot handle request due to validation error: {}", exception.getMessage());
    val message = exception.getConstraintViolations().stream()
        .map(ConstraintViolation::getMessage)
        .collect(joining(" "));
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ErrorRecord.of(ConstraintViolationException.class.getSimpleName(), message));
  }

  @ExceptionHandler(MalformedSearchSyntaxException.class)
  protected ResponseEntity<ErrorRecord> handleMalformedSearchSyntaxException(MalformedSearchSyntaxException exception) {
    log.info("Cannot handle request due to search syntax error: {}", exception.getMessage());
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ErrorRecord.of(MalformedSearchSyntaxException.class.getSimpleName(), exception.getMessage()));
  }
}
