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
     * Username parameter for the login form.
     */
    public static final String USERNAME_PARAM = "username";

    /**
     * Password parameter for the login form.
     */
    public static final String PASSWORD_PARAM = "password";

    /**
     * URL used for logging into the application.
     */
    public static final String SECURITY_CHECK_URI = "/j_spring_security_check";

    /**
     * String prefix added to JWT tokens in the Authorization header.
     */
    public static final String JWT_TOKEN_PREFIX = "Bearer ";

    /**
     * JWT claim used to store user's global roles in the system.
     */
    public static final String JWT_ROLE_CLAIM = "role";

    /**
     * Delimiter used to separate roles in a JWT.
     */
    public static final String JWT_ROLE_DELIMITER = "-";

    /**
     * Session timeout in milliseconds. 24 hours.
     */
    public static final int SESSION_TIMEOUT = 24 * 60 * 60 * 1000;

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
