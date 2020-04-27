package com.github.sgov.server.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sgov.server.environment.Generator;
import com.github.sgov.server.environment.config.TestSecurityConfig;
import com.github.sgov.server.model.UserAccount;
import com.github.sgov.server.security.model.AuthenticationToken;
import com.github.sgov.server.security.model.LoginStatus;
import com.github.sgov.server.security.model.SGoVUserDetails;
import com.github.sgov.server.service.BaseServiceTestRunner;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ContextConfiguration;

@Tag("security")
@ContextConfiguration(classes = {TestSecurityConfig.class})
class AuthenticationSuccessTest extends BaseServiceTestRunner {

  private UserAccount person = Generator.generateUserAccount();

  @Autowired
  private AuthenticationSuccess success;

  @Autowired
  private ObjectMapper mapper;

  static MockHttpServletRequest request() {
    return new MockHttpServletRequest();
  }

  static MockHttpServletResponse response() {
    return new MockHttpServletResponse();
  }

  @Test
  void authenticationSuccessReturnsResponseContainingUsername() throws Exception {
    final MockHttpServletResponse response = response();
    success.onAuthenticationSuccess(request(), response, generateAuthenticationToken());
    verifyLoginStatus(response);
  }

  private void verifyLoginStatus(MockHttpServletResponse response) throws java.io.IOException {
    final LoginStatus status =
        mapper.readValue(response.getContentAsString(), LoginStatus.class);
    assertTrue(status.isSuccess());
    assertTrue(status.isLoggedIn());
    assertEquals(person.getUsername(), status.getUsername());
    assertNull(status.getErrorMessage());
  }

  private Authentication generateAuthenticationToken() {
    final SGoVUserDetails userDetails = new SGoVUserDetails(person);
    return new AuthenticationToken(userDetails.getAuthorities(), userDetails);
  }

  @Test
  void logoutSuccessReturnsResponseContainingLoginStatus() throws Exception {
    final MockHttpServletResponse response = response();
    success.onLogoutSuccess(request(), response, generateAuthenticationToken());
    final LoginStatus status =
        mapper.readValue(response.getContentAsString(), LoginStatus.class);
    assertTrue(status.isSuccess());
    assertFalse(status.isLoggedIn());
    assertNull(status.getUsername());
    assertNull(status.getErrorMessage());
  }
}