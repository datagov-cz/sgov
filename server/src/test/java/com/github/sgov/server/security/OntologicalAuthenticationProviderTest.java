package com.github.sgov.server.security;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.github.sgov.server.dao.UserAccountDao;
import com.github.sgov.server.environment.Generator;
import com.github.sgov.server.model.UserAccount;
import com.github.sgov.server.security.model.SGoVUserDetails;
import com.github.sgov.server.service.BaseServiceTestRunner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;

@Tag("security")
@ContextConfiguration(classes = {OntologicalAuthenticationProvider.class, Listener.class})
class OntologicalAuthenticationProviderTest extends BaseServiceTestRunner {

  @Autowired
  private AuthenticationProvider provider;

  @Autowired
  private UserAccountDao userAccountDao;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @SpyBean
  private Listener listener;

  private UserAccount user;
  private String plainPassword;

  private static Authentication authentication(String username, String password) {
    return new UsernamePasswordAuthenticationToken(username, password);
  }

  @BeforeEach
  void setUp() {
    this.user = Generator.generateUserAccountWithPassword();
    this.plainPassword = user.getPassword();
    user.setPassword(passwordEncoder.encode(plainPassword));
    transactional(() -> userAccountDao.persist(user));
    SecurityContextHolder.setContext(new SecurityContextImpl());
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.setContext(new SecurityContextImpl());
  }

  @Test
  void successfulAuthenticationSetsSecurityContext() {
    final Authentication auth = authentication(user.getUsername(), plainPassword);
    final SecurityContext context = SecurityContextHolder.getContext();
    assertNull(context.getAuthentication());
    final Authentication result = provider.authenticate(auth);
    assertNotNull(SecurityContextHolder.getContext());
    final SGoVUserDetails details =
        (SGoVUserDetails) SecurityContextHolder.getContext().getAuthentication().getDetails();
    assertEquals(user.getUsername(), details.getUsername());
    assertTrue(result.isAuthenticated());
  }

  @Test
  void authenticateThrowsUserNotFoundExceptionForUnknownUsername() {
    final Authentication auth = authentication("unknownUsername", user.getPassword());
    assertThrows(UsernameNotFoundException.class, () -> provider.authenticate(auth));
    final SecurityContext context = SecurityContextHolder.getContext();
    assertNull(context.getAuthentication());
  }

  @Test
  void authenticateThrowsBadCredentialsForInvalidPassword() {
    final Authentication auth = authentication(user.getUsername(), "unknownPassword");
    assertThrows(BadCredentialsException.class, () -> provider.authenticate(auth));
    final SecurityContext context = SecurityContextHolder.getContext();
    assertNull(context.getAuthentication());
  }

  @Test
  void supportsUsernameAndPasswordAuthentication() {
    assertTrue(provider.supports(UsernamePasswordAuthenticationToken.class));
  }

  @Test
  void authenticateThrowsAuthenticationExceptionForEmptyUsername() {
    final Authentication auth = authentication("", "");
    final UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
        () -> provider.authenticate(auth));
    assertThat(ex.getMessage(), containsString("Username cannot be empty."));
  }

  @Test
  void successfulLoginEmitsLoginSuccessEvent() {
    final Authentication auth = authentication(user.getUsername(), plainPassword);
    provider.authenticate(auth);
    verify(listener).onSuccess(any());
    assertEquals(user, listener.getUser());
  }

  @Test
  void failedLoginEmitsLoginFailureEvent() {
    final Authentication auth = authentication(user.getUsername(), "unknownPassword");
    assertThrows(BadCredentialsException.class, () -> provider.authenticate(auth));
    verify(listener).onFailure(any());
    assertEquals(user, listener.getUser());
  }

  @Test
  void authenticateThrowsLockedExceptionForLockedUser() {
    user.lock();
    transactional(() -> userAccountDao.update(user));
    final Authentication auth = authentication(user.getUsername(), plainPassword);
    final LockedException ex =
        assertThrows(LockedException.class, () -> provider.authenticate(auth));
    assertEquals("Account of user " + user + " is locked.", ex.getMessage());
  }

  @Test
  void authenticationThrowsDisabledExceptionForDisabledUser() {
    user.disable();
    transactional(() -> userAccountDao.update(user));
    final Authentication auth = authentication(user.getUsername(), plainPassword);
    final DisabledException ex =
        assertThrows(DisabledException.class, () -> provider.authenticate(auth));
    assertEquals("Account of user " + user + " is disabled.", ex.getMessage());
  }
}