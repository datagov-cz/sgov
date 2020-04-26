package com.github.sgov.server.controller;

import com.github.sgov.server.controller.util.RestUtils;
import com.github.sgov.server.model.UserAccount;
import com.github.sgov.server.service.UserService;
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
 * Allows registration for anyone.
 */
@RestController
@RequestMapping("/users")
@Profile("!admin-registration-only")
@Api(tags = "Free Registration")
public class FreeRegistrationController {

  private static final Logger LOG = LoggerFactory.getLogger(FreeRegistrationController.class);

  private final UserService userService;

  @Autowired
  public FreeRegistrationController(UserService userService) {
    this.userService = userService;
    LOG.debug("Instantiating free registration controller.");
  }

  /**
   * Creates a new user.
   */
  @PreAuthorize("permitAll()")
  @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE, RestUtils.MEDIA_TYPE_JSONLD})
  public ResponseEntity<Void> createUser(@RequestBody UserAccount user) {
    userService.persist(user);
    LOG.info("User {} successfully registered.", user);
    return new ResponseEntity<>(HttpStatus.CREATED);
  }
}
