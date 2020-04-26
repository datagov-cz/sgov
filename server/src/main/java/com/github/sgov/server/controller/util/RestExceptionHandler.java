package com.github.sgov.server.controller.util;

import com.github.sgov.server.controller.dto.ErrorInfo;
import com.github.sgov.server.exception.AuthorizationException;
import com.github.sgov.server.exception.NotFoundException;
import com.github.sgov.server.exception.PersistenceException;
import com.github.sgov.server.exception.SGoVException;
import com.github.sgov.server.exception.ValidationException;
import cz.cvut.kbss.jopa.exceptions.OWLPersistenceException;
import cz.cvut.kbss.jsonld.exception.JsonLdException;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class RestExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(RestExceptionHandler.class);

  private static void logException(Throwable ex) {
    logException("Exception caught.", ex);
  }

  private static void logException(String message, Throwable ex) {
    LOG.error(message, ex);
  }

  private static ErrorInfo errorInfo(HttpServletRequest request, Throwable e) {
    return ErrorInfo.createWithMessage(e.getMessage(), request.getRequestURI());
  }

  @ExceptionHandler(PersistenceException.class)
  public ResponseEntity<ErrorInfo> persistenceException(HttpServletRequest request,
                                                        PersistenceException e) {
    logException(e);
    return new ResponseEntity<>(errorInfo(request, e.getCause()), HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(OWLPersistenceException.class)
  public ResponseEntity<ErrorInfo> jopaException(HttpServletRequest request,
                                                 OWLPersistenceException e) {
    logException("Persistence exception caught.", e);
    return new ResponseEntity<>(errorInfo(request, e), HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /**
   * notFound.
   */
  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ErrorInfo> notFound(HttpServletRequest request,
                                            NotFoundException e) {
    // Not necessary to log NotFoundException, they may be quite frequent and do not represent an
    // issue with the application
    return new ResponseEntity<>(errorInfo(request, e), HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(UsernameNotFoundException.class)
  public ResponseEntity<ErrorInfo> usernameNotFound(HttpServletRequest request,
                                                    UsernameNotFoundException e) {
    return new ResponseEntity<>(errorInfo(request, e), HttpStatus.NOT_FOUND);
  }


  @ExceptionHandler(AuthorizationException.class)
  public ResponseEntity<ErrorInfo> authorizationException(HttpServletRequest request,
                                                          AuthorizationException e) {
    logException(e);
    return new ResponseEntity<>(errorInfo(request, e), HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<ErrorInfo> validationException(HttpServletRequest request,
                                                       ValidationException e) {
    logException(e);
    return new ResponseEntity<>(errorInfo(request, e), HttpStatus.CONFLICT);
  }

  @ExceptionHandler(SGoVException.class)
  public ResponseEntity<ErrorInfo> termItException(HttpServletRequest request,
                                                   SGoVException e) {
    logException(e);
    return new ResponseEntity<>(errorInfo(request, e), HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /**
   * jsonLdException.
   */
  @ExceptionHandler(JsonLdException.class)
  public ResponseEntity<ErrorInfo> jsonLdException(HttpServletRequest request, JsonLdException e) {
    logException(e);
    return new ResponseEntity<>(
        ErrorInfo.createWithMessage("Error when processing JSON-LD.",
            request.getRequestURI()),
        HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(UnsupportedOperationException.class)
  public ResponseEntity<ErrorInfo> unsupportedAssetOperationException(
      HttpServletRequest request,
      UnsupportedOperationException e) {
    logException(e);
    return new ResponseEntity<>(errorInfo(request, e), HttpStatus.CONFLICT);
  }
}
