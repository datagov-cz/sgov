package com.github.sgov.server.exception;

/**
 * Indicates that a feature disabled by configuration was accessed.
 */
public class FeatureDisabledException  extends SGoVException {
    public FeatureDisabledException(String message) {
        super(message);
    }
}
