package com.github.sgov.server.controller;

import com.github.sgov.server.security.SecurityConstants;
import com.github.sgov.server.service.IdentifierResolver;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Base for application REST controllers.
 *
 * <p>Will be used to define general security for the public API.
 */
@PreAuthorize("hasRole('" + SecurityConstants.ROLE_USER + "')")
public class BaseController {

    protected final IdentifierResolver idResolver;

    protected BaseController(IdentifierResolver idResolver) {
        this.idResolver = idResolver;
    }
}
