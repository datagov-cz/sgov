package com.github.sgov.server.controller;

import com.github.sgov.server.controller.dto.UserUpdateDto;
import com.github.sgov.server.controller.util.RestUtils;
import com.github.sgov.server.model.UserAccount;
import com.github.sgov.server.security.SecurityConstants;
import com.github.sgov.server.service.IdentifierResolver;
import com.github.sgov.server.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.spring.web.json.Json;

@RestController
@RequestMapping("/users")
@Api(tags = "Users")
@SuppressWarnings("checkstyle:MissingJavadocType")
public class UserController extends BaseController {

    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    @Autowired
    public UserController(UserService userService, IdentifierResolver idResolver) {
        super(idResolver);
        this.userService = userService;
    }

    @PreAuthorize("hasRole('" + SecurityConstants.ROLE_ADMIN + "')")
    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE, RestUtils.MEDIA_TYPE_JSONLD})
    public List<UserAccount> getAll() {
        return userService.findAll();
    }

    @GetMapping(value = "/current", produces = {MediaType.APPLICATION_JSON_VALUE,
        RestUtils.MEDIA_TYPE_JSONLD})
    public UserAccount getCurrent() {
        return userService.getCurrent();
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping(value = "/current",
        consumes = {MediaType.APPLICATION_JSON_VALUE, RestUtils.MEDIA_TYPE_JSONLD})
    @ApiOperation(value = "Updates the current user. Note that all fields must be present. Also, "
        + "'uri' and 'username' must correspond to those of the current user.")
    @ApiImplicitParam(name = "update",
        required = true,
        paramType = "body",
        dataTypeClass = Json.class,
        value = "{\n"
            + "        \"uri\":\"http://onto.fel.cvut"
            + ".cz/ontologies/slovnik/agendovy/popis-dat/uživatel/franta-vomacka\",\n"
            + "        \"username\" : \"franta.vomacka@mujma.il\",\n"
            + "        \"lastName\" : \"Vomáčka\",\n"
            + "        \"firstName\" : \"Franta\"\n"
            + "    }"
    )
    public void updateCurrent(@RequestBody UserUpdateDto update) {
        userService.updateCurrent(update);
        LOG.debug("User {} successfully updated.", update);
    }

    /**
     * Deletes lock (unlocks) the user.
     *
     * @param identifierFragment fragment of user id
     * @param newPassword        new password to unlock the account
     */
    @PreAuthorize("hasRole('" + SecurityConstants.ROLE_ADMIN + "')")
    @DeleteMapping(value = "/{fragment}/lock")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlock(@PathVariable(name = "fragment") String identifierFragment,
                       @RequestBody String newPassword) {
        final UserAccount user = getUserAccountForUpdate(identifierFragment);
        userService.unlock(user, newPassword);
        LOG.debug("User {} successfully unlocked.", user);
    }

    private UserAccount getUserAccountForUpdate(String identifierFragment) {
        final URI id = idResolver.resolveUserIdentifier(identifierFragment);
        return userService.findRequired(id);
    }

    /**
     * Enables a (disabled) user.
     *
     * @param identifierFragment fragment of user id
     */
    @PreAuthorize("hasRole('" + SecurityConstants.ROLE_ADMIN + "')")
    @PostMapping(value = "/{fragment}/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void enable(@PathVariable(name = "fragment") String identifierFragment) {
        final UserAccount user = getUserAccountForUpdate(identifierFragment);
        userService.enable(user);
        LOG.debug("User {} successfully enabled.", user);
    }

    /**
     * Disables a user.
     *
     * @param identifierFragment fragment of user identifier
     */
    @PreAuthorize("hasRole('" + SecurityConstants.ROLE_ADMIN + "')")
    @DeleteMapping(value = "/{fragment}/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disable(@PathVariable(name = "fragment") String identifierFragment) {
        final UserAccount user = getUserAccountForUpdate(identifierFragment);
        userService.disable(user);
        LOG.debug("User {} successfully disabled.", user);
    }

    @PreAuthorize("permitAll()")
    @GetMapping(value = "/username")
    public Boolean exists(@RequestParam(name = "username") String username) {
        return userService.exists(username);
    }
}
