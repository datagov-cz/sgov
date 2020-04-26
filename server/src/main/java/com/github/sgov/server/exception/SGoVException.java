package com.github.sgov.server.exception;

/**
 * Application-specific exception.
 *
 * <p>All exceptions related to the application should be subclasses of this one.
 */
public class SGoVException extends RuntimeException {

  protected SGoVException() {
  }

  public SGoVException(String message) {
    super(message);
  }

  public SGoVException(String message, Throwable cause) {
    super(message, cause);
  }

  public SGoVException(Throwable cause) {
    super(cause);
  }
}
