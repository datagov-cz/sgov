package com.github.sgov.server.security;

/**
 * Security-related constants.
 */
public final class SecurityConstants {

    /**
     * Cookie used for the remember-me function.
     */
    public static final String REMEMBER_ME_COOKIE_NAME = "remember-me";

    /**
     * Maximum number of unsuccessful login attempts.
     */
    public static final int MAX_LOGIN_ATTEMPTS = 5;

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
