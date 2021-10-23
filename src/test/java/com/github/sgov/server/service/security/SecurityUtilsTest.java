package com.github.sgov.server.service.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.sgov.server.environment.Environment;
import com.github.sgov.server.environment.Generator;
import com.github.sgov.server.model.UserAccount;
import com.github.sgov.server.security.SecurityConstants;
import com.github.sgov.server.service.BaseServiceTestRunner;
import java.util.Collections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.springsecurity.account.SimpleKeycloakAccount;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

class SecurityUtilsTest extends BaseServiceTestRunner {

    private UserAccount user;

    @BeforeEach
    void setUp() {
        this.user = Generator.generateUserAccountWithPassword();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUserReturnsCurrentlyLoggedInUser() {
        Environment.setCurrentUser(user);
        final UserAccount result = SecurityUtils.getCurrentUser();
        assertEquals(user, result);
    }

    @Test
    void getCurrentUserSupportsExtractingCurrentUserFromKeycloakToken() {
        setKeycloakToken();
        final UserAccount result = SecurityUtils.getCurrentUser();
        assertEquals(user.getUsername(), result.getUsername());
    }

    private void setKeycloakToken() {
        final AccessToken token = new AccessToken();
        token.setSubject(user.getUri().toString());
        token.setGivenName(user.getFirstName());
        token.setFamilyName(user.getLastName());
        token.setEmail(user.getUsername());
        token.setPreferredUsername(user.getUsername());
        final KeycloakPrincipal<KeycloakSecurityContext> kp =
            new KeycloakPrincipal<>(user.getUsername(),
                new KeycloakSecurityContext(null, token, null, null));
        SecurityContext context = new SecurityContextImpl();
        context.setAuthentication(
            new KeycloakAuthenticationToken(new SimpleKeycloakAccount(kp, Collections.singleton(
                SecurityConstants.ROLE_USER), null), true,
                Collections.singleton(new SimpleGrantedAuthority(SecurityConstants.ROLE_USER))));
        SecurityContextHolder.setContext(context);
    }

    @Test
    void isAuthenticatedWorksInStaticVersion() {
        Environment.setCurrentUser(user);
        assertTrue(SecurityUtils.authenticated());
    }
}