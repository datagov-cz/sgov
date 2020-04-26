package com.github.sgov.server.security;

import com.github.sgov.server.config.conf.JwtConf;
import com.github.sgov.server.exception.IncompleteJwtException;
import com.github.sgov.server.exception.JwtException;
import com.github.sgov.server.exception.TokenExpiredException;
import com.github.sgov.server.model.UserAccount;
import com.github.sgov.server.security.model.SGoVUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class JwtUtils {

  private JwtConf config;

  @Autowired
  public JwtUtils(JwtConf config) {
    this.config = config;
  }

  private static String mapAuthoritiesToClaim(Collection<? extends GrantedAuthority> authorities) {
    return authorities.stream().map(GrantedAuthority::getAuthority)
        .collect(Collectors.joining(SecurityConstants.JWT_ROLE_DELIMITER));
  }

  private static void verifyAttributePresence(Claims claims) {
    if (claims.getSubject() == null) {
      throw new IncompleteJwtException("JWT is missing subject.");
    }
    if (claims.getId() == null) {
      throw new IncompleteJwtException("JWT is missing id.");
    }
    if (claims.getExpiration() == null) {
      throw new TokenExpiredException("Missing token expiration info. Assuming expired.");
    }
  }

  private static List<GrantedAuthority> mapClaimToAuthorities(String claim) {
    if (claim == null) {
      return Collections.emptyList();
    }
    final String[] roles = claim.split(SecurityConstants.JWT_ROLE_DELIMITER);
    final List<GrantedAuthority> authorities = new ArrayList<>(roles.length);
    for (String role : roles) {
      authorities.add(new SimpleGrantedAuthority(role));
    }
    return authorities;
  }

  /**
   * Generates a JSON Web Token for the specified authenticated user.
   *
   * @param userDetails User info
   * @return Generated JWT has
   */
  public String generateToken(SGoVUserDetails userDetails) {
    final Date issued = new Date();
    return Jwts.builder().setSubject(userDetails.getUsername())
        .setId(userDetails.getUser().getUri().toString())
        .setIssuedAt(issued)
        .setExpiration(new Date(issued.getTime() + SecurityConstants.SESSION_TIMEOUT))
        .claim(SecurityConstants.JWT_ROLE_CLAIM,
            mapAuthoritiesToClaim(userDetails.getAuthorities()))
        .signWith(SignatureAlgorithm.HS512, config.getSecretKey())
        .compact();
  }

  /**
   * Retrieves user info from the specified JWT.
   *
   * <p>The token is first validated for correct format and expiration date.
   *
   * @param token JWT to read
   * @return User info retrieved from the specified token
   */
  public SGoVUserDetails extractUserInfo(String token) {
    Objects.requireNonNull(token);
    try {
      final Claims claims = getClaimsFromToken(token);
      verifyAttributePresence(claims);
      final UserAccount user = new UserAccount();
      user.setUri(URI.create(claims.getId()));
      user.setUsername(claims.getSubject());
      final String roles = claims.get(SecurityConstants.JWT_ROLE_CLAIM, String.class);
      return new SGoVUserDetails(user, mapClaimToAuthorities(roles));
    } catch (IllegalArgumentException e) {
      throw new JwtException("Unable to parse user identifier from the specified JWT.", e);
    }
  }

  private Claims getClaimsFromToken(String token) {
    try {
      return Jwts.parser().setSigningKey(config.getSecretKey())
          .parseClaimsJws(token).getBody();
    } catch (MalformedJwtException e) {
      throw new JwtException("Unable to parse the specified JWT.", e);
    } catch (SignatureException e) {
      throw new JwtException("Invalid signature of the specified JWT.", e);
    } catch (ExpiredJwtException e) {
      throw new TokenExpiredException(e.getMessage());
    }
  }

  /**
   * Updates issuing and expiration date of the specified token, generating a new one.
   *
   * @param token The token to refresh
   * @return Newly generated token with updated expiration date
   */
  public String refreshToken(String token) {
    Objects.requireNonNull(token);
    final Claims claims = getClaimsFromToken(token);
    final Date issuedAt = new Date();
    claims.setIssuedAt(issuedAt);
    claims.setExpiration(new Date(issuedAt.getTime() + SecurityConstants.SESSION_TIMEOUT));
    return Jwts.builder().setClaims(claims)
        .signWith(SignatureAlgorithm.HS512, config.getSecretKey()).compact();
  }
}
