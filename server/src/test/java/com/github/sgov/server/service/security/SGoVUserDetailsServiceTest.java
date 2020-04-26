package com.github.sgov.server.service.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.sgov.server.environment.Generator;
import com.github.sgov.server.model.UserAccount;
import com.github.sgov.server.security.model.SGoVUserDetails;
import com.github.sgov.server.service.BaseServiceTestRunner;
import cz.cvut.kbss.jopa.model.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

class SGoVUserDetailsServiceTest extends BaseServiceTestRunner {

  @Autowired
  private EntityManager em;

  @Autowired
  private SGoVUserDetailsService sut;

  @Test
  void loadUserByUsernameReturnsUserDetailsForLoadedUser() {
    final UserAccount user = Generator.generateUserAccountWithPassword();
    transactional(() -> em.persist(user));

    final SGoVUserDetails result = sut.loadUserByUsername(user.getUsername());
    assertNotNull(result);
    assertEquals(user, result.getUser());
  }

  @Test
  void loadUserByUsernameThrowsUsernameNotFoundForUnknownUsername() {
    final String username = "unknownUsername";
    final UsernameNotFoundException ex =
        assertThrows(UsernameNotFoundException.class, () -> sut.loadUserByUsername(username));
    assertEquals("User with username " + username + " not found.", ex.getMessage());
  }
}