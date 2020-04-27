package com.github.sgov.server.security.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Status of the login attempt.
 */
@Setter
@Getter
@Accessors(chain = true)
public class LoginStatus {

  private boolean loggedIn;
  private String username;
  private String errorMessage;
  /**
   * Represents identifier of the error, which can be resolved to a localized message in the JS UI.
   */
  private String errorId;
  private boolean success;
}
