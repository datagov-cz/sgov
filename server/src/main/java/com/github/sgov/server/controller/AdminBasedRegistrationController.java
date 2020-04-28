package com.github.sgov.server.controller;

import com.github.sgov.server.model.UserAccount;
import com.github.sgov.server.security.SecurityConstants;
import com.github.sgov.server.service.UserService;
import cz.cvut.kbss.jsonld.JsonLd;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Allows only administrators to register new users.
 */
@RestController
@RequestMapping("/users")
@Profile("admin-registration-only")
@Api(tags = "Admin Based Registration")
public class AdminBasedRegistrationController {

    private static final Logger LOG = LoggerFactory.getLogger(FreeRegistrationController.class);

    private final UserService userService;

    @Autowired
    public AdminBasedRegistrationController(UserService userService) {
        this.userService = userService;
        LOG.debug("Instantiating admin-based registration controller.");
    }

    /**
     * Creates a new user.
     */
    @PreAuthorize("hasRole('" + SecurityConstants.ROLE_ADMIN + "')")
    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE, JsonLd.MEDIA_TYPE})
    public ResponseEntity<Void> createUser(@RequestBody UserAccount user) {
        userService.persist(user);
        LOG.info("User {} successfully registered.", user);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
