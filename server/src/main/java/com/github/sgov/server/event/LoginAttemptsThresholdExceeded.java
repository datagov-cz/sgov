package com.github.sgov.server.event;

import com.github.sgov.server.model.UserAccount;

/**
 * Event emitted when a user exceeds the maximum number
 * ({@link com.github.sgov.server.security.SecurityConstants#MAX_LOGIN_ATTEMPTS})
 * of unsuccessful login attempts.
 */
public class LoginAttemptsThresholdExceeded extends UserEvent {

  public LoginAttemptsThresholdExceeded(UserAccount user) {
    super(user);
  }
}
