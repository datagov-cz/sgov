package com.github.sgov.server.exception;

/**
 * Indicates that a resource was not found.
 */
public class NotFoundException extends SGoVException {

  public NotFoundException(String message) {
    super(message);
  }

  public static NotFoundException create(String resourceName, Object identifier) {
    return new NotFoundException(resourceName + " identified by " + identifier + " not found.");
  }
}
