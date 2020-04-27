package com.github.sgov.server.service.security;

import com.github.sgov.server.dao.UserAccountDao;
import com.github.sgov.server.security.model.SGoVUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Service for fetching user details.
 */
@Service
public class SGoVUserDetailsService implements UserDetailsService {

  private final UserAccountDao userAccountDao;

  @Autowired
  public SGoVUserDetailsService(UserAccountDao userAccountDao) {
    this.userAccountDao = userAccountDao;
  }

  @Override
  public SGoVUserDetails loadUserByUsername(String username) {
    return new SGoVUserDetails(userAccountDao.findByUsername(username).orElseThrow(
        () -> new UsernameNotFoundException("User with username " + username + " not found.")));
  }
}
