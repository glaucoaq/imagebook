package com.company.imagebook.exceptions;

public class ConflictException extends IllegalArgumentException {

  public ConflictException(String message, Throwable cause) {
    super(message, cause);
  }
}
