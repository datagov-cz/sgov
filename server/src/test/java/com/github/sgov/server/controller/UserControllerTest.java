package com.github.sgov.server.controller;

import static com.github.sgov.server.environment.Generator.generateUserAccount;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.sgov.server.environment.Environment;
import com.github.sgov.server.model.UserAccount;
import com.github.sgov.server.service.UserService;
import com.github.sgov.server.util.Vocabulary;
import java.util.Collections;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MvcResult;

class UserControllerTest extends BaseControllerTestRunner {

    private static final String BASE_URL = "/users";

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController sut;

    private UserAccount user;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        super.setUp(sut);
        this.user = generateUserAccount();
        this.user.setTypes(Collections.singleton(Vocabulary.s_c_administrator));
        Environment.setCurrentUser(user);
    }

    @AfterEach
    void tearDown() throws Exception{
        mocks.close();
    }

    @Test
    void getCurrentReturnsUserWithoutTypes() throws Exception {
        when(userService.getCurrent()).thenReturn(user);
        final MvcResult mvcResult =
            mockMvc.perform(get(BASE_URL + "/current"))
                .andExpect(status().isOk())
                .andReturn();

        final JSONObject o = new JSONObject(mvcResult.getResponse().getContentAsString());
        assertFalse(o.has("types"));
        assertTrue(o.has("username"));
    }
}
