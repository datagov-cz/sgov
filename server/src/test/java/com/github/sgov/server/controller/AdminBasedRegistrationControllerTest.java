package com.github.sgov.server.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import com.github.sgov.server.util.Vocabulary;
import java.util.List;
import javax.servlet.Filter;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(initializers = ConfigFileApplicationContextInitializer.class, classes = {
    TestRestSecurityConfig.class,
    AdminBasedRegistrationControllerTest.Config.class,
    JwtConf.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@WebAppConfiguration
@ActiveProfiles("test")
class AdminBasedRegistrationControllerTest extends BaseControllerTestRunner {

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
    void createUserPersistsUserWhenCalledByAdmin() throws Exception {
        final UserAccount admin = Generator.generateUserAccount();
        admin.addType(Vocabulary.s_c_administrator);
        Environment.setCurrentUser(admin);
        final UserAccount user = Generator.generateUserAccount();
        mockMvc
            .perform(
                post("/users").content(toJson(user)).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isCreated());
        verify(userService).persist(user);
    }

    @Test
    void createUserThrowsForbiddenForNonAdminUser() throws Exception {
        final UserAccount admin = Generator.generateUserAccount();
        Environment.setCurrentUser(admin);
        final UserAccount user = Generator.generateUserAccount();
        mockMvc
            .perform(
                post("/users").content(toJson(user)).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isForbidden());
        verify(userService, never()).persist(any());
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
        private AdminBasedRegistrationController controller;

        Config() {
            MockitoAnnotations.initMocks(this);
        }

        @Bean
        public UserService userService() {
            return userService;
        }

        @Bean
        public AdminBasedRegistrationController registrationController() {
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
