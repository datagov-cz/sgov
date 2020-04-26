package com.github.sgov.server.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sgov.server.environment.config.TestSecurityConfig;
import com.github.sgov.server.security.model.LoginStatus;
import com.github.sgov.server.service.BaseServiceTestRunner;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ContextConfiguration;

@Tag("security")
@ContextConfiguration(classes = {TestSecurityConfig.class})
class AuthenticationFailureTest extends BaseServiceTestRunner {

  @Autowired
  private AuthenticationFailure failure;

  @Autowired
  private ObjectMapper mapper;

  @Test
  void authenticationFailureReturnsLoginStatusWithErrorInfoOnUsernameNotFound() throws Exception {
    final MockHttpServletRequest request = AuthenticationSuccessTest.request();
    final MockHttpServletResponse response = AuthenticationSuccessTest.response();
    final String msg = "Username not found";
    final AuthenticationException e = new UsernameNotFoundException(msg);
    failure.onAuthenticationFailure(request, response, e);
    final LoginStatus status = mapper.readValue(response.getContentAsString(), LoginStatus.class);
    assertFalse(status.isSuccess());
    assertFalse(status.isLoggedIn());
    assertNull(status.getUsername());
    assertEquals(msg, status.getErrorMessage());
    assertEquals("login.error", status.getErrorId());
  }

  @Test
  void authenticationFailureReturnsLoginStatusWithErrorInfoOnAccountLocked() throws Exception {
    final MockHttpServletRequest request = AuthenticationSuccessTest.request();
    final MockHttpServletResponse response = AuthenticationSuccessTest.response();
    final String msg = "Account is locked.";
    failure.onAuthenticationFailure(request, response, new LockedException(msg));
    final LoginStatus status = mapper.readValue(response.getContentAsString(), LoginStatus.class);
    assertFalse(status.isSuccess());
    assertFalse(status.isLoggedIn());
    assertNull(status.getUsername());
    assertEquals(msg, status.getErrorMessage());
    assertEquals("login.locked", status.getErrorId());
  }

  @Test
  void authenticationFailureReturnsLoginStatusWithErrorInfoOnAccountDisabled() throws Exception {
    final MockHttpServletRequest request = AuthenticationSuccessTest.request();
    final MockHttpServletResponse response = AuthenticationSuccessTest.response();
    final String msg = "Account is disabled.";
    failure.onAuthenticationFailure(request, response, new DisabledException(msg));
    final LoginStatus status = mapper.readValue(response.getContentAsString(), LoginStatus.class);
    assertFalse(status.isSuccess());
    assertFalse(status.isLoggedIn());
    assertNull(status.getUsername());
    assertEquals(msg, status.getErrorMessage());
    assertEquals("login.disabled", status.getErrorId());
  }
}