package com.github.sgov.server.exception;

/**
 * General exception for issues with JSON Web Tokens.
 */
public class JwtException extends SGoVException {

  public JwtException(String message) {
    super(message);
  }

  public JwtException(String message, Throwable cause) {
    super(message, cause);
  }
}
