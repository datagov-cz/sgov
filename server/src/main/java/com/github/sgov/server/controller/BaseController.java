package com.github.sgov.server.controller;

import com.github.sgov.server.controller.util.RestUtils;
import com.github.sgov.server.exception.ValidationException;
import com.github.sgov.server.model.util.HasIdentifier;
import com.github.sgov.server.security.SecurityConstants;
import com.github.sgov.server.service.IdentifierResolver;
import com.github.sgov.server.util.Constants.QueryParams;
import org.springframework.security.access.prepost.PreAuthorize;

import java.net.URI;

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

    URI generateLocation(URI identifier, String entityType) {
        if (identifier.toString().startsWith(getEntityTypeNamespace(entityType))) {
            return RestUtils.createLocationFromCurrentUriWithPath("/{name}",
                    IdentifierResolver.extractIdentifierFragment(identifier));
        } else {
            return RestUtils.createLocationFromCurrentUriWithPathAndQuery("/{name}", QueryParams.NAMESPACE,
                    IdentifierResolver.extractIdentifierNamespace(identifier),
                    IdentifierResolver.extractIdentifierFragment(identifier));
        }
    }

    /**
     * Resolves identifier based on the specified resource (if provided) or the namespace loaded from application
     * configuration.
     *
     * @param namespace  Explicitly provided namespace. Optional
     * @param fragment   Locally unique identifier fragment
     * @param entityType Used in {@code namespace} if not specified
     * @return Resolved identifier
     */
    protected URI resolveIdentifier(String namespace, String fragment, String entityType) {
        if (namespace != null) {
            return idResolver.resolveIdentifier(namespace, fragment);
        } else {
            return idResolver.resolveIdentifier(getEntityTypeNamespace(entityType), fragment);
        }
    }

    /**
     * Ensures that the entity specified for update has the same identifier as the one that has been resolved from the
     * request URL.
     *
     * @param entity            Entity
     * @param requestIdentifier Identifier resolved from request
     */
    void verifyRequestAndEntityIdentifier(HasIdentifier entity, URI requestIdentifier) {
        if (!requestIdentifier.equals(entity.getUri())) {
            throw new ValidationException(
                    "The ID " + requestIdentifier +
                            ", resolved from request URL, does not match the ID of the specified entity.");
        }
    }


    private String getEntityTypeNamespace(String entityType) {
        return entityType + "/";
    }

}
