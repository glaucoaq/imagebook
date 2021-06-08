package com.company.imagebook.services.exceptions;

public class ConflictException extends IllegalArgumentException {

  public ConflictException(String message, Throwable cause) {
    super(message, cause);
  }
}
