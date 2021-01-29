package com.github.sgov.server.controller;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import com.github.sgov.server.config.conf.JwtConf;
import com.github.sgov.server.controller.util.RestExceptionHandler;
import com.github.sgov.server.environment.Environment;
import com.github.sgov.server.environment.Generator;
import com.github.sgov.server.environment.config.TestRestSecurityConfig;
import com.github.sgov.server.model.UserAccount;
import com.github.sgov.server.security.JwtUtils;
import com.github.sgov.server.service.UserService;
import com.github.sgov.server.service.security.SecurityUtils;
import java.util.Collections;
import java.util.List;
import javax.servlet.Filter;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * This tests only the security aspect of {@link UserController}. Functionality is tested in {@link
 * UserControllerTest}.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(initializers = ConfigFileApplicationContextInitializer.class,
    classes = {
        JwtConf.class,
        TestRestSecurityConfig.class,
        UserControllerSecurityTest.Config.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@WebAppConfiguration
@ActiveProfiles("test")
class UserControllerSecurityTest extends BaseControllerTestRunner {

    private static final String BASE_URL = "/users";

    @Autowired
    private Filter springSecurityFilterChain;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        super.setupObjectMappers();
        // WebApplicationContext is required for proper security. Otherwise, standaloneSetup
        // could be
        // used
        this.mockMvc =
            MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity(springSecurityFilterChain))
                .build();
    }

    @Test
    void findAllThrowsForbiddenForUnauthorizedUser() throws Exception {
        Environment.setCurrentUser(Generator.generateUserAccountWithPassword());
        when(userService.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/users")).andExpect(status().isForbidden());
        verify(userService, never()).findAll();
    }

    @Test
    void getCurrentReturnsCurrentlyLoggedInUser() throws Exception {
        final UserAccount user = Generator.generateUserAccountWithPassword();
        Environment.setCurrentUser(user);
        when(userService.getCurrent()).thenReturn(user);
        final MvcResult mvcResult =
            mockMvc.perform(get(BASE_URL + "/current").accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk()).andReturn();
        final UserAccount result = readValue(mvcResult, UserAccount.class);
        Assert.assertEquals(user, result);
    }

    /**
     * Inner class is necessary to provide the controller as a bean, so that the
     * WebApplicationContext can map it.
     */
    @EnableWebMvc
    @Configuration
    public static class Config implements WebMvcConfigurer {
        @Mock
        private UserService userService;

        @Mock
        private SecurityUtils securityUtilsMock;

        @InjectMocks
        private UserController controller;

        Config() {
            MockitoAnnotations.initMocks(this);
        }

        @Bean
        public UserService userService() {
            return userService;
        }

        @Bean
        public UserController userController() {
            return controller;
        }

        @Bean
        public SecurityUtils securityUtils() {
            return securityUtilsMock;
        }

        @Bean
        public RestExceptionHandler restExceptionHandler() {
            return new RestExceptionHandler();
        }

        @Bean
        public JwtUtils jwtUtils(JwtConf config) {
            return new JwtUtils(config);
        }

        @Override
        public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
            converters.add(Environment.createJsonLdMessageConverter());
            converters.add(Environment.createDefaultMessageConverter());
            converters.add(Environment.createStringEncodingMessageConverter());
        }
    }
}
