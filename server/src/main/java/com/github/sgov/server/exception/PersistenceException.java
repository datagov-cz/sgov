package com.github.sgov.server.exception;

/**
 * Marks an exception that occurred in the persistence layer.
 */
public class PersistenceException extends SGoVException {

    public PersistenceException(Throwable cause) {
        super(cause);
    }

}
