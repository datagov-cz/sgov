package com.github.sgov.server.security;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sgov.server.config.conf.JwtConf;
import com.github.sgov.server.controller.dto.ErrorInfo;
import com.github.sgov.server.environment.Environment;
import com.github.sgov.server.environment.Generator;
import com.github.sgov.server.model.UserAccount;
import com.github.sgov.server.security.model.SGoVUserDetails;
import com.github.sgov.server.service.security.SGoVUserDetailsService;
import com.github.sgov.server.service.security.SecurityUtils;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import javax.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Tag("security")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(initializers = ConfigFileApplicationContextInitializer.class,
    classes = {JwtConf.class})
@ActiveProfiles("test")
class JwtAuthorizationFilterTest {

  @Autowired
  private JwtConf config;

  private UserAccount user;

  private MockHttpServletRequest mockRequest = new MockHttpServletRequest();

  private MockHttpServletResponse mockResponse = new MockHttpServletResponse();

  @Mock
  private FilterChain chainMock;

  @Mock
  private AuthenticationManager authManagerMock;

  @Mock
  private SGoVUserDetailsService detailsServiceMock;

  @Mock
  private SecurityUtils securityUtilsMock;

  private JwtUtils jwtUtilsSpy;

  private ObjectMapper objectMapper;

  private JwtAuthorizationFilter sut;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
    this.user = Generator.generateUserAccount();
    this.jwtUtilsSpy = spy(new JwtUtils(config));
    this.objectMapper = Environment.getObjectMapper();
    this.sut = new JwtAuthorizationFilter(authManagerMock, jwtUtilsSpy, securityUtilsMock,
        detailsServiceMock,
        objectMapper);
    when(detailsServiceMock.loadUserByUsername(user.getUsername()))
        .thenReturn(new SGoVUserDetails(user));
  }

  @Test
  void doFilterInternalExtractsUserInfoFromJwtAndSetsUpSecurityContext() throws Exception {
    generateJwtIntoRequest();

    sut.doFilterInternal(mockRequest, mockResponse, chainMock);
    final ArgumentCaptor<SGoVUserDetails> captor =
        ArgumentCaptor.forClass(SGoVUserDetails.class);
    verify(securityUtilsMock).setCurrentUser(captor.capture());
    final SGoVUserDetails userDetails = captor.getValue();
    assertEquals(user, userDetails.getUser());
  }

  private void generateJwtIntoRequest() {
    final String token = generateJwt();
    mockRequest
        .addHeader(HttpHeaders.AUTHORIZATION, SecurityConstants.JWT_TOKEN_PREFIX + token);
  }

  private String generateJwt() {
    return Jwts.builder().setSubject(user.getUsername())
        .setId(user.getUri().toString())
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + 10000))
        .signWith(SignatureAlgorithm.HS512, config.getSecretKey()).compact();
  }

  @Test
  void doFilterInternalInvokesFilterChainAfterSuccessfulExtractionOfUserInfo() throws Exception {
    generateJwtIntoRequest();
    sut.doFilterInternal(mockRequest, mockResponse, chainMock);
    verify(chainMock).doFilter(mockRequest, mockResponse);
  }

  @Test
  void doFilterInternalLeavesEmptySecurityContextAndPassesRequestFurtherWhenMissingAuthentication()
      throws Exception {
    sut.doFilterInternal(mockRequest, mockResponse, chainMock);
    verify(chainMock).doFilter(mockRequest, mockResponse);
    verify(securityUtilsMock, never()).setCurrentUser(any());
  }

  @Test
  void doFilterInternalLeavesEmptySecurityContextAndPassesRequestForAuthenticationInWrongFormat()
      throws Exception {
    mockRequest.addHeader(HttpHeaders.AUTHORIZATION, generateJwt());
    sut.doFilterInternal(mockRequest, mockResponse, chainMock);
    verify(chainMock).doFilter(mockRequest, mockResponse);
    verify(securityUtilsMock, never()).setCurrentUser(any());
  }

  @Test
  void doFilterInternalRefreshesUserTokenOnSuccessfulAuthorization() throws Exception {
    generateJwtIntoRequest();
    sut.doFilterInternal(mockRequest, mockResponse, chainMock);
    assertTrue(mockResponse.containsHeader(HttpHeaders.AUTHORIZATION));
    assertNotEquals(mockRequest.getHeader(HttpHeaders.AUTHORIZATION),
        mockResponse.getHeader(HttpHeaders.AUTHORIZATION));
    verify(jwtUtilsSpy).refreshToken(any());
  }

  @Test
  void doFilterInternalReturnsUnauthorizedWhenWhenTokenIsExpired() throws Exception {
    final String token = Jwts.builder().setSubject(user.getUsername())
        .setId(user.getUri().toString())
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() - 10000))
        .signWith(SignatureAlgorithm.HS512, config.getSecretKey()).compact();
    mockRequest
        .addHeader(HttpHeaders.AUTHORIZATION, SecurityConstants.JWT_TOKEN_PREFIX + token);
    sut.doFilterInternal(mockRequest, mockResponse, chainMock);
    assertEquals(HttpStatus.UNAUTHORIZED.value(), mockResponse.getStatus());
    final ErrorInfo errorInfo =
        objectMapper.readValue(mockResponse.getContentAsString(), ErrorInfo.class);
    assertNotNull(errorInfo);
    assertThat(errorInfo.getMessage(), containsString("expired"));
  }

  @Test
  void doFilterInternalReturnsUnauthorizedWhenUserAccountIsLocked() throws Exception {
    generateJwtIntoRequest();
    user.lock();
    sut.doFilterInternal(mockRequest, mockResponse, chainMock);
    assertEquals(HttpStatus.UNAUTHORIZED.value(), mockResponse.getStatus());
    final ErrorInfo errorInfo =
        objectMapper.readValue(mockResponse.getContentAsString(), ErrorInfo.class);
    assertNotNull(errorInfo);
    assertThat(errorInfo.getMessage(), containsString("locked"));
  }

  @Test
  void doFilterInternalReturnsUnauthorizedWhenUserAccountIsDisabled() throws Exception {
    generateJwtIntoRequest();
    user.disable();
    sut.doFilterInternal(mockRequest, mockResponse, chainMock);
    assertEquals(HttpStatus.UNAUTHORIZED.value(), mockResponse.getStatus());
    final ErrorInfo errorInfo =
        objectMapper.readValue(mockResponse.getContentAsString(), ErrorInfo.class);
    assertNotNull(errorInfo);
    assertThat(errorInfo.getMessage(), containsString("disabled"));
  }

  @Test
  void doFilterInternalReturnsUnauthorizedOnIncompleteJwtToken() throws Exception {
    // Missing id
    final String token = Jwts.builder().setSubject(user.getUsername())
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + 10000))
        .signWith(SignatureAlgorithm.HS512, config.getSecretKey()).compact();
    mockRequest
        .addHeader(HttpHeaders.AUTHORIZATION, SecurityConstants.JWT_TOKEN_PREFIX + token);
    sut.doFilterInternal(mockRequest, mockResponse, chainMock);
    assertEquals(HttpStatus.UNAUTHORIZED.value(), mockResponse.getStatus());
    final ErrorInfo errorInfo =
        objectMapper.readValue(mockResponse.getContentAsString(), ErrorInfo.class);
    assertNotNull(errorInfo);
    assertThat(errorInfo.getMessage(), containsString("missing"));
  }

  @Test
  void doFilterInternalReturnsUnauthorizedOnUnparseableUserInfoInJwtToken() throws Exception {
    // Missing id
    final String token = Jwts.builder().setSubject(user.getUsername())
        .setId(":1235")    // Not valid URI
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + 10000))
        .signWith(SignatureAlgorithm.HS512, config.getSecretKey()).compact();
    mockRequest
        .addHeader(HttpHeaders.AUTHORIZATION, SecurityConstants.JWT_TOKEN_PREFIX + token);
    sut.doFilterInternal(mockRequest, mockResponse, chainMock);
    assertEquals(HttpStatus.UNAUTHORIZED.value(), mockResponse.getStatus());
    final ErrorInfo errorInfo =
        objectMapper.readValue(mockResponse.getContentAsString(), ErrorInfo.class);
    assertNotNull(errorInfo);
  }

  @Test
  void doFilterInternalReturnsUnauthorizedForUnknownUserInToken() throws Exception {
    final String token = Jwts.builder().setSubject("unknownUser")
        .setId(Generator.generateUri().toString())
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + 10000))
        .signWith(SignatureAlgorithm.HS512, config.getSecretKey()).compact();
    when(detailsServiceMock.loadUserByUsername(anyString()))
        .thenThrow(UsernameNotFoundException.class);
    mockRequest
        .addHeader(HttpHeaders.AUTHORIZATION, SecurityConstants.JWT_TOKEN_PREFIX + token);
    sut.doFilterInternal(mockRequest, mockResponse, chainMock);
    assertEquals(HttpStatus.UNAUTHORIZED.value(), mockResponse.getStatus());
  }
}