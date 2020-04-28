package com.github.sgov.server.event;

import com.github.sgov.server.model.UserAccount;

/**
 * Emitted when a user login attempt fails.
 */
public class LoginFailureEvent extends UserEvent {

    public LoginFailureEvent(UserAccount user) {
        super(user);
    }
}
