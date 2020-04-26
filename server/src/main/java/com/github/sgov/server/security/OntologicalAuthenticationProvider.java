package com.github.sgov.server.security;

import com.github.sgov.server.event.LoginFailureEvent;
import com.github.sgov.server.event.LoginSuccessEvent;
import com.github.sgov.server.model.UserAccount;
import com.github.sgov.server.security.model.AuthenticationToken;
import com.github.sgov.server.security.model.SGoVUserDetails;
import com.github.sgov.server.service.security.SGoVUserDetailsService;
import com.github.sgov.server.service.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class OntologicalAuthenticationProvider
    implements AuthenticationProvider, ApplicationEventPublisherAware {

  private static final Logger LOG =
      LoggerFactory.getLogger(OntologicalAuthenticationProvider.class);

  private final SecurityUtils securityUtils;

  private final SGoVUserDetailsService userDetailsService;

  private final PasswordEncoder passwordEncoder;

  private ApplicationEventPublisher eventPublisher;

  /**
   * OntologicalAuthenticationProvider.
   */
  @Autowired
  public OntologicalAuthenticationProvider(SecurityUtils securityUtils,
                                           SGoVUserDetailsService userDetailsService,
                                           PasswordEncoder passwordEncoder) {
    this.securityUtils = securityUtils;
    this.userDetailsService = userDetailsService;
    this.passwordEncoder = passwordEncoder;
  }

  private static void verifyUsernameNotEmpty(String username) {
    if (username.isEmpty()) {
      throw new UsernameNotFoundException("Username cannot be empty.");
    }
  }

  @Override
  public Authentication authenticate(Authentication authentication) {
    final String username = authentication.getPrincipal().toString();
    verifyUsernameNotEmpty(username);
    LOG.debug("Authenticating user {}", username);

    final SGoVUserDetails userDetails = userDetailsService.loadUserByUsername(username);
    SecurityUtils.verifyAccountStatus(userDetails.getUser());
    final String password = (String) authentication.getCredentials();
    if (!passwordEncoder.matches(password, userDetails.getPassword())) {
      onLoginFailure(userDetails.getUser());
      throw new BadCredentialsException("Provided credentials don't match.");
    }
    onLoginSuccess(userDetails.getUser());
    return securityUtils.setCurrentUser(userDetails);
  }

  private void onLoginFailure(UserAccount user) {
    user.erasePassword();
    eventPublisher.publishEvent(new LoginFailureEvent(user));
  }

  private void onLoginSuccess(UserAccount user) {
    eventPublisher.publishEvent(new LoginSuccessEvent(user));
  }

  @Override
  public boolean supports(Class<?> cls) {
    return UsernamePasswordAuthenticationToken.class.isAssignableFrom(cls)
        || AuthenticationToken.class.isAssignableFrom(cls);
  }

  @Override
  public void setApplicationEventPublisher(ApplicationEventPublisher eventPublisher) {
    this.eventPublisher = eventPublisher;
  }
}
