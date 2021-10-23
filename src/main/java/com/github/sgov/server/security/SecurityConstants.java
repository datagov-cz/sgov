package com.github.sgov.server.security;

/**
 * Security-related constants.
 */
public final class SecurityConstants {

    /**
     * System administrator role.
     */
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    /**
     * Regular system user role.
     */
    public static final String ROLE_USER = "ROLE_USER";

    private SecurityConstants() {
        throw new AssertionError();
    }
}
