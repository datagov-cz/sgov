package com.github.sgov.server.security;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.sgov.server.config.conf.JwtConf;
import com.github.sgov.server.environment.Generator;
import com.github.sgov.server.exception.IncompleteJwtException;
import com.github.sgov.server.exception.JwtException;
import com.github.sgov.server.exception.TokenExpiredException;
import com.github.sgov.server.model.UserAccount;
import com.github.sgov.server.security.model.SGoVUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Tag("security")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(initializers = ConfigFileApplicationContextInitializer.class,
    classes = {JwtConf.class
    })
@ActiveProfiles("test")
class JwtUtilsTest {

  private static final List<String> ROLES = Arrays.asList("USER", "ADMIN");

  @Autowired
  private JwtConf config;

  private UserAccount user;

  private JwtUtils sut;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
    this.user = Generator.generateUserAccount();
    this.sut = new JwtUtils(config);
  }

  @Test
  void generateTokenCreatesJwtForUserWithoutAuthorities() {
    final SGoVUserDetails userDetails = new SGoVUserDetails(user);
    final String jwtToken = sut.generateToken(userDetails);
    verifyJwtToken(jwtToken, userDetails);
  }

  private void verifyJwtToken(String token, SGoVUserDetails userDetails) {
    final Claims claims =
        Jwts.parser().setSigningKey(config.getSecretKey()).parseClaimsJws(token)
            .getBody();
    assertEquals(user.getUsername(), claims.getSubject());
    assertEquals(user.getUri().toString(), claims.getId());
    assertThat(claims.getExpiration(), greaterThan(claims.getIssuedAt()));
    if (!userDetails.getAuthorities().isEmpty()) {
      assertTrue(claims.containsKey(SecurityConstants.JWT_ROLE_CLAIM));
      final String[] roles = claims.get(SecurityConstants.JWT_ROLE_CLAIM, String.class)
          .split(SecurityConstants.JWT_ROLE_DELIMITER);
      for (String role : roles) {
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority(role)));
      }
    }
  }

  @Test
  void generateTokenCreatesJwtForUserWithAuthorities() {
    final Set<GrantedAuthority> authorities = ROLES.stream().map(SimpleGrantedAuthority::new)
        .collect(Collectors.toSet());
    final SGoVUserDetails userDetails = new SGoVUserDetails(user, authorities);
    final String jwtToken = sut.generateToken(userDetails);
    verifyJwtToken(jwtToken, userDetails);
  }

  @Test
  void extractUserInfoExtractsDataOfUserWithoutAuthoritiesFromJwt() {
    final String token = Jwts.builder().setSubject(user.getUsername())
        .setId(user.getUri().toString())
        .setIssuedAt(new Date())
        .setExpiration(
            new Date(System.currentTimeMillis() + SecurityConstants.SESSION_TIMEOUT))
        .signWith(SignatureAlgorithm.HS512, config.getSecretKey()).compact();

    final SGoVUserDetails result = sut.extractUserInfo(token);
    assertEquals(user, result.getUser());
    assertEquals(1, result.getAuthorities().size());
    assertTrue(result.getAuthorities().contains(SGoVUserDetails.DEFAULT_AUTHORITY));
  }

  @Test
  void extractUserInfoExtractsDataOfUserWithAuthoritiesFromJwt() {
    final String token = Jwts.builder().setSubject(user.getUsername())
        .setId(user.getUri().toString())
        .setIssuedAt(new Date())
        .setExpiration(
            new Date(System.currentTimeMillis() + SecurityConstants.SESSION_TIMEOUT))
        .claim(SecurityConstants.JWT_ROLE_CLAIM,
            String.join(SecurityConstants.JWT_ROLE_DELIMITER, ROLES))
        .signWith(SignatureAlgorithm.HS512, config.getSecretKey()).compact();

    final SGoVUserDetails result = sut.extractUserInfo(token);
    ROLES.forEach(
        r -> assertTrue(result.getAuthorities().contains(new SimpleGrantedAuthority(r))));
  }

  @Test
  void extractUserInfoThrowsJwtExceptionWhenTokenCannotBeParsed() {
    final String token = "bblablalbla";
    final JwtException ex = assertThrows(JwtException.class, () -> sut.extractUserInfo(token));
    assertThat(ex.getMessage(), containsString("Unable to parse the specified JWT."));
  }

  @Test
  void extractUserInfoThrowsJwtExceptionWhenUserIdentifierIsNotValidUri() {
    final String token = Jwts.builder().setSubject(user.getUsername())
        .setId("_:123")
        .setIssuedAt(new Date())
        .setExpiration(
            new Date(System.currentTimeMillis() + SecurityConstants.SESSION_TIMEOUT))
        .signWith(SignatureAlgorithm.HS512, config.getSecretKey()).compact();
    assertThrows(JwtException.class, () -> sut.extractUserInfo(token));
  }

  @Test
  void extractUserInfoThrowsIncompleteJwtExceptionWhenUsernameIsMissing() {
    final String token = Jwts.builder().setId(user.getUri().toString())
        .setIssuedAt(new Date())
        .setExpiration(
            new Date(System.currentTimeMillis() + SecurityConstants.SESSION_TIMEOUT))
        .signWith(SignatureAlgorithm.HS512, config.getSecretKey()).compact();
    final IncompleteJwtException ex =
        assertThrows(IncompleteJwtException.class, () -> sut.extractUserInfo(token));
    assertThat(ex.getMessage(), containsString("subject"));
  }

  @Test
  void extractUserInfoThrowsIncompleteJwtExceptionWhenIdentifierIsMissing() {
    final String token = Jwts.builder().setSubject(user.getUsername())
        .setIssuedAt(new Date())
        .setExpiration(
            new Date(System.currentTimeMillis() + SecurityConstants.SESSION_TIMEOUT))
        .signWith(SignatureAlgorithm.HS512, config.getSecretKey()).compact();
    final IncompleteJwtException ex =
        assertThrows(IncompleteJwtException.class, () -> sut.extractUserInfo(token));
    assertThat(ex.getMessage(), containsString("id"));
  }

  @Test
  void extractUserInfoThrowsTokenExpiredExceptionWhenExpirationIsInPast() {
    final String token = Jwts.builder().setId(user.getUri().toString())
        .setSubject(user.getUsername())
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() - 1000))
        .signWith(SignatureAlgorithm.HS512, config.getSecretKey()).compact();
    assertThrows(TokenExpiredException.class, () -> sut.extractUserInfo(token));
  }

  @Test
  void extractUserInfoThrowsTokenExpiredExceptionWhenExpirationIsMissing() {
    final String token = Jwts.builder().setId(user.getUri().toString())
        .setSubject(user.getUsername())
        .setIssuedAt(new Date())
        .signWith(SignatureAlgorithm.HS512, config.getSecretKey()).compact();
    assertThrows(TokenExpiredException.class, () -> sut.extractUserInfo(token));
  }

  @Test
  void refreshTokenUpdatesIssuedDate() {
    final Date oldIssueDate = new Date(System.currentTimeMillis() - 10000);
    final String token = Jwts.builder().setSubject(user.getUsername())
        .setId(user.getUri().toString())
        .setIssuedAt(oldIssueDate)
        .setExpiration(new Date(oldIssueDate.getTime() + SecurityConstants.SESSION_TIMEOUT))
        .signWith(SignatureAlgorithm.HS512, config.getSecretKey()).compact();

    final String result = sut.refreshToken(token);
    final Claims claims =
        Jwts.parser().setSigningKey(config.getSecretKey()).parseClaimsJws(result)
            .getBody();
    assertTrue(claims.getIssuedAt().after(oldIssueDate));
  }

  @Test
  void refreshTokenUpdatesExpirationDate() {
    final Date oldIssueDate = new Date();
    final Date oldExpiration = new Date(oldIssueDate.getTime() + 10000);
    final String token = Jwts.builder().setSubject(user.getUsername())
        .setId(user.getUri().toString())
        .setIssuedAt(oldIssueDate)
        .setExpiration(oldExpiration)
        .signWith(SignatureAlgorithm.HS512, config.getSecretKey()).compact();

    final String result = sut.refreshToken(token);
    final Claims claims =
        Jwts.parser().setSigningKey(config.getSecretKey()).parseClaimsJws(result)
            .getBody();
    assertTrue(claims.getExpiration().after(oldExpiration));
  }

  @Test
  void extractUserInfoThrowsJwtExceptionWhenTokenIsSignedWithInvalidSecret() {
    final String token = Jwts.builder().setSubject(user.getUsername())
        .setId(user.getUri().toString())
        .setIssuedAt(new Date())
        .setExpiration(
            new Date(System.currentTimeMillis() + SecurityConstants.SESSION_TIMEOUT))
        .signWith(SignatureAlgorithm.HS512, "differentSecret").compact();

    assertThrows(JwtException.class, () -> sut.extractUserInfo(token));
  }
}