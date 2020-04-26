package com.github.sgov.server.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.github.sgov.server.config.conf.JwtConf;
import com.github.sgov.server.environment.Generator;
import com.github.sgov.server.model.UserAccount;
import com.github.sgov.server.security.model.AuthenticationToken;
import com.github.sgov.server.security.model.SGoVUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import java.util.Collections;
import javax.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Tag("security")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(initializers = ConfigFileApplicationContextInitializer.class,
    classes = {JwtConf.class})
@ActiveProfiles("test")
class JwtAuthenticationFilterTest {

  @Autowired
  private JwtConf config;

  private MockHttpServletRequest mockRequest;

  private MockHttpServletResponse mockResponse;

  private UserAccount user;

  @Mock
  private FilterChain filterChain;

  private JwtAuthenticationFilter sut;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
    this.user = Generator.generateUserAccount();
    this.mockRequest = new MockHttpServletRequest();
    this.mockResponse = new MockHttpServletResponse();
    this.sut = new JwtAuthenticationFilter(mock(AuthenticationManager.class), new JwtUtils(config));
  }

  @Test
  void successfulAuthenticationAddsJwtToResponse() throws Exception {
    final AuthenticationToken token =
        new AuthenticationToken(Collections.emptySet(), new SGoVUserDetails(user));
    sut.successfulAuthentication(mockRequest, mockResponse, filterChain, token);
    assertTrue(mockResponse.containsHeader(HttpHeaders.AUTHORIZATION));
    final String value = mockResponse.getHeader(HttpHeaders.AUTHORIZATION);
    assertNotNull(value);
    assertTrue(value.startsWith(SecurityConstants.JWT_TOKEN_PREFIX));
    final String jwtToken = value.substring(SecurityConstants.JWT_TOKEN_PREFIX.length());
    final Jws<Claims> jwt = Jwts.parser().setSigningKey(config.getSecretKey())
        .parseClaimsJws(jwtToken);
    assertFalse(jwt.getBody().isEmpty());
  }
}