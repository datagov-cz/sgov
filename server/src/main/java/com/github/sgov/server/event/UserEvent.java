package com.github.sgov.server.event;

import com.github.sgov.server.model.UserAccount;
import java.util.Objects;

/**
 * Base class for user-related events.
 */
abstract class UserEvent {

  private final UserAccount user;

  UserEvent(UserAccount user) {
    this.user = Objects.requireNonNull(user);
  }

  /**
   * Gets the user who is concerned by this event.
   *
   * @return User
   */
  public UserAccount getUser() {
    return user;
  }
}
