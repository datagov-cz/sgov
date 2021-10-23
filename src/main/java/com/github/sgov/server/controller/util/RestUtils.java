package com.github.sgov.server.controller.util;

import java.net.URI;
import java.util.Objects;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Utility functions for request processing.
 */
public final class RestUtils {

    public static final String MEDIA_TYPE_JSONLD = "application/ld+json";

    private RestUtils() {
        throw new AssertionError();
    }

    /**
     * Creates location URI with the specified path appended to the current request URI.
     *
     * <p>The {@code uriVariableValues} are used to fill in possible variables specified in
     * {@code path}.
     *
     * @param path              Path to add to the current request URI in order to construct a
     *                          resource location
     * @param uriVariableValues Values used to replace possible variables in the path
     * @return location {@code URI}
     */
    public static URI createLocationFromCurrentUriWithPath(String path,
                                                           Object... uriVariableValues) {
        Objects.requireNonNull(path);
        return ServletUriComponentsBuilder.fromCurrentRequestUri().path(path).buildAndExpand(
            uriVariableValues).toUri();
    }

    /**
     * Creates location URI with the specified path and query parameter appended to the current
     * request URI.
     *
     * <p>The {@code paramValue} is specified for the query parameter and {@code pathValues} are
     * used to replace path variables.
     *
     * @param path       Path string, may contain path variables
     * @param param      Query parameter to add to current request URI
     * @param paramValue Value of the query parameter
     * @param pathValues Path variable values
     * @return location {@code URI}
     * @see #createLocationFromCurrentUriWithPath(String, Object...)
     */
    public static URI createLocationFromCurrentUriWithPathAndQuery(String path, String param,
                                                                   Object paramValue,
                                                                   Object... pathValues) {
        Objects.requireNonNull(path);
        Objects.requireNonNull(param);
        return ServletUriComponentsBuilder.fromCurrentRequestUri().queryParam(param, paramValue)
            .path(path).buildAndExpand(pathValues).toUri();
    }
}
