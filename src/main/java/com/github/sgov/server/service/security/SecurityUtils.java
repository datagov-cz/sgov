package com.github.sgov.server.service.security;

import com.github.sgov.server.model.UserAccount;
import com.github.sgov.server.security.model.AuthenticationToken;
import com.github.sgov.server.security.model.SGoVUserDetails;
import com.github.sgov.server.security.model.UserRole;
import com.github.sgov.server.service.IdentifierResolver;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.representations.AccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Handle user session-related functions.
 */
@Service
public class SecurityUtils {

    /**
     * SecurityUtils.
     */
    @Autowired
    public SecurityUtils() {
        // Ensures security context is propagated to additionally spun threads, e.g., used
        // by @Async methods
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }

    /**
     * Checks if a user is currently authenticated, or if the current thread is processing a request
     * from an anonymous user.
     *
     * @return Whether a user is authenticated
     */
    public static boolean authenticated() {
        final SecurityContext context = SecurityContextHolder.getContext();
        return context.getAuthentication() != null && context.getAuthentication().isAuthenticated();
    }

    /**
     * Gets the currently authenticated user.
     *
     * @return Current user
     */
    public static UserAccount getCurrentUser() {
        final SecurityContext context = SecurityContextHolder.getContext();
        assert context != null && context.getAuthentication().isAuthenticated();
        if (context.getAuthentication().getPrincipal() instanceof KeycloakPrincipal) {
            return resolveAccountFromKeycloakPrincipal(context);
        } else {
            assert context.getAuthentication() instanceof AuthenticationToken;
            return ((SGoVUserDetails) context.getAuthentication().getDetails()).getUser();
        }
    }

    private static UserAccount resolveAccountFromKeycloakPrincipal(SecurityContext context) {
        final KeycloakPrincipal<?> principal =
            (KeycloakPrincipal<?>) context.getAuthentication().getPrincipal();
        final AccessToken keycloakToken = principal.getKeycloakSecurityContext().getToken();
        final UserAccount account = new UserAccount();
        account.setFirstName(keycloakToken.getGivenName());
        account.setLastName(keycloakToken.getFamilyName());
        account.setUsername(keycloakToken.getPreferredUsername());
        context.getAuthentication().getAuthorities().stream()
            .map(ga -> UserRole.fromRoleName(ga.getAuthority()))
            .filter(r -> !r.getType().isEmpty()).forEach(r -> account.addType(r.getType()));
        account.setUri(IdentifierResolver
            .generateUserIdentifier(keycloakToken.getSubject()));
        return account;
    }
}
