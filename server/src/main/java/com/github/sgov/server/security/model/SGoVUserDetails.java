package com.github.sgov.server.security.model;

import com.github.sgov.server.model.UserAccount;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class SGoVUserDetails implements UserDetails {

  /**
   * Default authority held by all registered users of the system.
   */
  public static final GrantedAuthority DEFAULT_AUTHORITY =
      new SimpleGrantedAuthority(UserRole.USER.getName());
  private final Set<GrantedAuthority> authorities;
  private final UserAccount user;

  /**
   * SGoVUserDetails.
   */
  public SGoVUserDetails(UserAccount user) {
    Objects.requireNonNull(user);
    this.user = user;
    this.authorities = resolveAuthorities(user);
  }

  /**
   * SGoVUserDetails.
   */
  public SGoVUserDetails(UserAccount user, Collection<GrantedAuthority> authorities) {
    Objects.requireNonNull(user);
    Objects.requireNonNull(authorities);
    this.user = user;
    this.authorities = resolveAuthorities(user);
    this.authorities.addAll(authorities);
  }

  private static Set<GrantedAuthority> resolveAuthorities(UserAccount user) {
    final Set<GrantedAuthority> authorities = new HashSet<>(4);
    authorities.add(DEFAULT_AUTHORITY);
    if (user.getTypes() != null) {
      authorities.addAll(user.getTypes().stream().filter(UserRole::exists)
          .map(r -> new SimpleGrantedAuthority(UserRole.fromType(r).getName()))
          .collect(Collectors.toSet()));
    }
    return authorities;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return Collections.unmodifiableCollection(authorities);
  }

  @Override
  public String getPassword() {
    return user.getPassword();
  }

  @Override
  public String getUsername() {
    return user.getUsername();
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return !user.isLocked();
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return user.isEnabled();
  }

  public UserAccount getUser() {
    return user.copy();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SGoVUserDetails)) {
      return false;
    }
    SGoVUserDetails that = (SGoVUserDetails) o;
    return Objects.equals(user, that.user) && Objects.equals(authorities, that.authorities);
  }

  @Override
  public int hashCode() {
    return Objects.hash(user, authorities);
  }

  @Override
  public String toString() {
    return "UserDetails{"
        + "user=" + user
        + ", authorities=" + authorities
        + '}';
  }
}
