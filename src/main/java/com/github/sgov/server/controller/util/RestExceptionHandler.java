package com.github.sgov.server.controller.util;

import com.github.sgov.server.controller.dto.ErrorInfo;
import com.github.sgov.server.exception.AuthorizationException;
import com.github.sgov.server.exception.NotFoundException;
import com.github.sgov.server.exception.PersistenceException;
import com.github.sgov.server.exception.PublicationException;
import com.github.sgov.server.exception.SGoVException;
import com.github.sgov.server.exception.ValidationException;
import com.github.sgov.server.exception.VocabularyRegisteredinReadWriteException;
import cz.cvut.kbss.jopa.exceptions.OWLPersistenceException;
import cz.cvut.kbss.jsonld.exception.JsonLdException;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Exception handlers for REST controllers.
 *
 * <p>The general pattern should be that unless an exception can be handled in a more appropriate
 * place it bubbles up to a REST controller which originally received the request. There, it is
 * caught by this handler, logged and a reasonable error message is returned to the user.
 */
@ControllerAdvice
@Slf4j
public class RestExceptionHandler {

    private static void logException(Throwable ex) {
        logException("Exception caught.", ex);
    }

    private static void logException(String message, Throwable ex) {
        log.error(message, ex);
    }

    private static ErrorInfo errorInfo(HttpServletRequest request, Throwable e) {
        return ErrorInfo.createWithMessage(e.getMessage(), request.getRequestURI());
    }

    /**
     * Persistence Exception.
     */
    @ExceptionHandler(PersistenceException.class)
    public ResponseEntity<ErrorInfo> persistenceException(HttpServletRequest request,
                                                          PersistenceException e) {
        logException(e);
        return new ResponseEntity<>(errorInfo(request, e.getCause()),
            HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * JOPA Exception.
     */
    @ExceptionHandler(OWLPersistenceException.class)
    public ResponseEntity<ErrorInfo> jopaException(HttpServletRequest request,
                                                   OWLPersistenceException e) {
        logException("Persistence exception caught.", e);
        return new ResponseEntity<>(errorInfo(request, e),
            HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Not Found Exception.
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorInfo> notFound(HttpServletRequest request,
                                              NotFoundException e) {
        // Not necessary to log NotFoundException, they may be quite frequent and do not
        // represent an
        // issue with the application
        return new ResponseEntity<>(errorInfo(request, e), HttpStatus.NOT_FOUND);
    }

    /**
     * Username not found.
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public ResponseEntity<ErrorInfo> usernameNotFound(HttpServletRequest request,
                                                      UsernameNotFoundException e) {
        return new ResponseEntity<>(errorInfo(request, e), HttpStatus.NOT_FOUND);
    }

    /**
     * Authorization exception.
     */
    @ExceptionHandler(AuthorizationException.class)
    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public ResponseEntity<ErrorInfo> authorizationException(HttpServletRequest request,
                                                            AuthorizationException e) {
        logException(e);
        return new ResponseEntity<>(errorInfo(request, e), HttpStatus.FORBIDDEN);
    }

    /**
     * Validation Exception.
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorInfo> validationException(HttpServletRequest request,
                                                         ValidationException e) {
        logException(e);
        return new ResponseEntity<>(errorInfo(request, e), HttpStatus.CONFLICT);
    }

    /**
     * SGoVException.
     */
    @ExceptionHandler(SGoVException.class)
    public ResponseEntity<ErrorInfo> sgovException(HttpServletRequest request,
                                                   SGoVException e) {
        logException(e);
        return new ResponseEntity<>(errorInfo(request, e), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * JSON-LD Exception.
     */
    @ExceptionHandler(JsonLdException.class)
    public ResponseEntity<ErrorInfo> jsonLdException(HttpServletRequest request,
                                                     JsonLdException e) {
        logException(e);
        return new ResponseEntity<>(
            ErrorInfo.createWithMessage("Error when processing JSON-LD.",
                request.getRequestURI()),
            HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Unsupported Asset Operation Exception.
     */
    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<ErrorInfo> unsupportedAssetOperationException(
        HttpServletRequest request,
        UnsupportedOperationException e) {
        logException(e);
        return new ResponseEntity<>(errorInfo(request, e), HttpStatus.CONFLICT);
    }

    /**
     * Publication Exception.
     */
    @ExceptionHandler(PublicationException.class)
    public ResponseEntity<ErrorInfo> publicationException(HttpServletRequest request,
                                                          PublicationException e) {
        logException("Publication exception caught.", e);
        return new ResponseEntity<>(errorInfo(request, e), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Vocabulary Already Registered for Editing Exception.
     */
    @ExceptionHandler(VocabularyRegisteredinReadWriteException.class)
    public ResponseEntity<ErrorInfo> vocabularyAlreadyRegisteredInReadWriteException(
        HttpServletRequest request,
        VocabularyRegisteredinReadWriteException e) {
        return new ResponseEntity<>(errorInfo(request, e), HttpStatus.CONFLICT);
    }
}
