package com.github.sgov.server.event;

import com.github.sgov.server.model.UserAccount;

/**
 * Emitted when a user successfully logs in.
 */
public class LoginSuccessEvent extends UserEvent {

  public LoginSuccessEvent(UserAccount user) {
    super(user);
  }
}
