package com.github.sgov.server.controller;

import com.github.sgov.server.controller.util.RestUtils;
import com.github.sgov.server.model.UserAccount;
import com.github.sgov.server.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@Api(tags = "Users")
@SuppressWarnings("checkstyle:MissingJavadocType")
public class UserController extends BaseController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(value = "/current", produces = {MediaType.APPLICATION_JSON_VALUE,
        RestUtils.MEDIA_TYPE_JSONLD})
    @ApiOperation(value = "Gets info about the currently logged-in user.")
    public UserAccount getCurrent() {
        return userService.getCurrent();
    }
}
