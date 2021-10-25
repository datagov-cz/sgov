package com.github.sgov.server.security.model;

import java.security.Principal;
import java.util.Collection;
import java.util.Objects;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

/**
 * Authentication token for given authorities and user.
 */
public class AuthenticationToken extends AbstractAuthenticationToken implements Principal {

    private final SGoVUserDetails userDetails;

    /**
     * AuthenticationToken.
     */
    public AuthenticationToken(Collection<? extends GrantedAuthority> authorities,
                               SGoVUserDetails userDetails) {
        super(authorities);
        this.userDetails = userDetails;
        super.setAuthenticated(true);
        super.setDetails(userDetails);
    }

    @Override
    public Object getCredentials() {
        return userDetails.getPassword();
    }

    @Override
    public Object getPrincipal() {
        return userDetails;
    }

    @Override
    public SGoVUserDetails getDetails() {
        return userDetails;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AuthenticationToken)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final AuthenticationToken that = (AuthenticationToken) o;
        return Objects.equals(userDetails, that.userDetails);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), userDetails);
    }

}
