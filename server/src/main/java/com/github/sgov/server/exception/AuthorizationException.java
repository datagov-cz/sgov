package com.github.sgov.server.exception;

/**
 * Indicates that a the user attempted to access a resources/function for which they have
 * insufficient authority.
 */
public class AuthorizationException extends SGoVException {

    public AuthorizationException(String message) {
        super(message);
    }
}
