package com.github.sgov.server.controller;

import static com.github.sgov.server.environment.Generator.generateUserAccount;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import com.fasterxml.jackson.core.type.TypeReference;
import com.github.sgov.server.environment.Environment;
import com.github.sgov.server.model.UserAccount;
import com.github.sgov.server.service.IdentifierResolver;
import com.github.sgov.server.service.UserService;
import com.github.sgov.server.util.Vocabulary;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

class UserControllerTest extends BaseControllerTestRunner {

    private static final String BASE_URL = "/users";

    @Mock
    private UserService userService;

    @Mock
    private IdentifierResolver idResolverMock;

    @InjectMocks
    private UserController sut;

    private UserAccount user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        super.setUp(sut);
        this.user = generateUserAccount();
        this.user.setTypes(Collections.singleton(Vocabulary.s_c_administrator));
        Environment.setCurrentUser(user);
    }

    @Test
    void getAllReturnsAllUsers() throws Exception {
        final List<UserAccount> users = IntStream.range(0, 5).mapToObj(i -> generateUserAccount())
            .collect(Collectors.toList());
        when(userService.findAll()).thenReturn(users);

        final MvcResult mvcResult =
            mockMvc.perform(get(BASE_URL).accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk()).andReturn();
        final List<UserAccount> result =
            readValue(mvcResult, new TypeReference<List<UserAccount>>() {
            });
        assertEquals(users, result);
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
    }
}
