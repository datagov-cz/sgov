package com.github.sgov.server.service.security;

import com.github.sgov.server.event.LoginFailureEvent;
import com.github.sgov.server.event.LoginSuccessEvent;
import org.springframework.context.event.EventListener;

/**
 * Tracks login attempts.
 */
public interface LoginTracker {

  /**
   * Registers an unsuccessful login attempt by the specified user.
   *
   * <p>This basically means that the user entered an incorrect password.
   *
   * @param event Event representing the login attempt
   */
  @EventListener
  void onLoginFailure(LoginFailureEvent event);

  /**
   * Registers a successful login attempt by the specified user.
   *
   * <p>This basically means that the user entered the correct password and will be logged in.
   *
   * @param event Event representing the login attempt
   */
  @EventListener
  void onLoginSuccess(LoginSuccessEvent event);
}
