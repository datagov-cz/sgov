package com.github.sgov.server.exception;

/**
 * Indicates that publication to GitHub failed.
 */
public class PublicationException extends SGoVException {

    public PublicationException(String message) {
        super(message);
    }

    public PublicationException(String message, Throwable t) {
        super(message, t);
    }
}
