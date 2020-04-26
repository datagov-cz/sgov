package com.github.sgov.server.exception;

/**
 * Indicates that the specified JWT does not contain all the required data.
 */
public class IncompleteJwtException extends JwtException {

  public IncompleteJwtException(String message) {
    super(message);
  }
}
