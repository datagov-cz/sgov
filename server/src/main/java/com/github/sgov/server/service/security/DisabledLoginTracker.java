package com.github.sgov.server.service.security;

import com.github.sgov.server.event.LoginFailureEvent;
import com.github.sgov.server.event.LoginSuccessEvent;

/**
 * Dummy login tracker which does nothing.
 */
public class DisabledLoginTracker implements LoginTracker {

  @Override
  public void onLoginFailure(LoginFailureEvent event) {
    // Do nothing
  }

  @Override
  public void onLoginSuccess(LoginSuccessEvent event) {
    // Do nothing
  }
}
