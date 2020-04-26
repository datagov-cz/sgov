package com.github.sgov.server.exception;

/**
 * Indicates that a user's authentication token has expired.
 */
public class TokenExpiredException extends JwtException {

  public TokenExpiredException(String message) {
    super(message);
  }
}
