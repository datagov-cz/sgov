package com.github.sgov.server.controller;

import com.github.sgov.server.controller.util.RestUtils;
import com.github.sgov.server.model.UserAccount;
import com.github.sgov.server.model.Workspace;
import com.github.sgov.server.security.SecurityConstants;
import com.github.sgov.server.service.UserService;
import com.github.sgov.server.util.Constants;
import com.github.sgov.server.util.Vocabulary;
import cz.cvut.kbss.jsonld.JsonLd;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.net.URI;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@Api(tags = "Users")
@SuppressWarnings("checkstyle:MissingJavadocType")
public class UserController extends BaseController {

    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasRole('" + SecurityConstants.ROLE_ADMIN + "')")
    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE, RestUtils.MEDIA_TYPE_JSONLD})
    @ApiOperation(value = "Lists all registered users (including disabled).")
    public List<UserAccount> getAll() {
        return userService.findAll();
    }

    @GetMapping(value = "/current", produces = {MediaType.APPLICATION_JSON_VALUE,
        RestUtils.MEDIA_TYPE_JSONLD})
    @ApiOperation(value = "Gets info about the currently logged-in user.")
    public UserAccount getCurrent() {
        return userService.getCurrent();
    }

    @GetMapping(value = "/current/workspace", produces = {
        MediaType.APPLICATION_JSON_VALUE, JsonLd.MEDIA_TYPE})
    @ApiOperation(value = "Retrieve current workspace of authenticated user.")
    @ResponseBody
    public Workspace getCurrentWorkspace() {
        return userService.getCurrentWorkspace();
    }

    @DeleteMapping(value = "/current/workspace")
    @ApiOperation(value = "Unset current workspace of authenticated user.")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unsetCurrentWorkspace() {
        LOG.debug("Workspace unset from current user.");
        userService.removeCurrentWorkspace();
    }

    /**
     * Set current workspace of authenticated user.
     *
     * @param workspaceFragment Localname of workspace id.
     * @param namespace         Namespace used for resource identifier resolution. Optional, if not
     *                          specified, the configured namespace is used.
     */
    @PutMapping(value = "/current/workspace/{workspaceFragment}", produces = {
        MediaType.APPLICATION_JSON_VALUE, JsonLd.MEDIA_TYPE})
    @ApiOperation(value = "Set current workspace of authenticated user.")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateCurrentWorkspace(
        @PathVariable String workspaceFragment,
        @RequestParam(name = Constants.QueryParams.NAMESPACE, required = false)
            String namespace) {
        final URI identifier = resolveIdentifier(
            namespace, workspaceFragment, Vocabulary.s_c_metadatovy_kontext);
        LOG.debug("Workspace {} set to current user.", identifier);
        userService.changeCurrentWorkspace(identifier);
    }
}
