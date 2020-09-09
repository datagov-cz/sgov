package com.github.sgov.server.exception;

import java.net.URI;
import java.util.Collection;

/**
 * Exception signalizing, that a vocabulary is already registered in R/W mode in another
 * workspace(s).
 */
public class VocabularyRegisteredinReadWriteException extends SGoVException {

    public VocabularyRegisteredinReadWriteException(String message) {
        super(message);
    }

    /**
     * Creates a new VocabularyRegisteredinReadWriteException.
     *
     * @param vocabulary vocabularyIRI that is in conflict
     * @param workspaces URIs of workspaces that contain the vocabulary in R/W
     * @return the exception.
     */
    public static VocabularyRegisteredinReadWriteException create(String vocabulary,
                                                                  Collection<URI> workspaces) {
        return new VocabularyRegisteredinReadWriteException(
            "Vocabulary " + vocabulary + " is already registered for editing in workspaces "
               + workspaces);
    }
}
