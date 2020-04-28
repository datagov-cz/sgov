package com.github.sgov.server.controller;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.sgov.server.environment.Generator;
import com.github.sgov.server.model.UserAccount;
import com.github.sgov.server.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;

class FreeRegistrationControllerTest extends BaseControllerTestRunner {

    @Mock
    private UserService userService;

    @InjectMocks
    private FreeRegistrationController sut;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        setUp(sut);
    }

    @Test
    void createUserPersistsUser() throws Exception {
        final UserAccount user = Generator.generateUserAccount();
        mockMvc
            .perform(
                post("/users").content(toJson(user)).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isCreated());
        verify(userService).persist(user);
    }
}
