package com.company.imagebook.services.exceptions;

public class MalformedSearchSyntaxException extends IllegalArgumentException {

  public MalformedSearchSyntaxException(String message) {
    super(message);
  }

  public MalformedSearchSyntaxException(String message, Throwable cause) {
    super(message, cause);
  }
}
