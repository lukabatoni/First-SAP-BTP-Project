package com.example.java_tutorial.exceptions;

public class SecondAppServiceException extends Exception {
  public SecondAppServiceException(String message) {
    super(message);
  }

  public SecondAppServiceException(String message, Throwable cause) {
    super(message, cause);
  }
}
