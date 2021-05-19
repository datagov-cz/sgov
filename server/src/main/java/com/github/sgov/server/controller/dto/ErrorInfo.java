package com.github.sgov.server.controller.dto;

import lombok.Data;

/**
 * Contains information about an error and can be send to client as JSON to let him know what is
 * wrong.
 */
@Data
public class ErrorInfo {

    private String message;

    /**
     * Gets error message identifier.
     *
     * <p>This identifier can be used by the UI to display a corresponding localized error message.
     *
     * @return Error message identifier
     */
    private String messageId;

    private String requestUri;

    public ErrorInfo(String requestUri) {
        this.requestUri = requestUri;
    }

    /**
     * Creates a new instance with the specified message and request URI.
     *
     * @param message    Error message
     * @param requestUri URI of the request which caused the error
     * @return New {@code ErrorInfo} instance
     */
    public static ErrorInfo createWithMessage(String message, String requestUri) {
        final ErrorInfo errorInfo = new ErrorInfo(requestUri);
        errorInfo.setMessage(message);
        return errorInfo;
    }
}
