package com.github.sgov.server.service.security;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.github.sgov.server.environment.Generator;
import com.github.sgov.server.event.LoginAttemptsThresholdExceeded;
import com.github.sgov.server.event.LoginFailureEvent;
import com.github.sgov.server.event.LoginSuccessEvent;
import com.github.sgov.server.model.UserAccount;
import com.github.sgov.server.security.SecurityConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Configuration
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RuntimeBasedLoginTrackerTest.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class RuntimeBasedLoginTrackerTest {

    @Autowired
    private LoginTracker loginTracker;
    @Autowired
    private LoginListener listener;
    private UserAccount user;

    @Bean
    public LoginTracker loginTracker() {
        return new RuntimeBasedLoginTracker();
    }

    @Bean
    public LoginListener loginListener() {
        return spy(new LoginListener());
    }

    @BeforeEach
    void setUp() {
        this.user = Generator.generateUserAccount();
    }

    @Test
    void emitsThresholdExceededEventWhenMaximumLoginCountIsExceeded() {
        for (int i = 0; i < SecurityConstants.MAX_LOGIN_ATTEMPTS; i++) {
            assertNull(listener.user);
            loginTracker.onLoginFailure(new LoginFailureEvent(user));
        }
        loginTracker.onLoginFailure(new LoginFailureEvent(user));
        assertNotNull(listener.user);
        assertEquals(user, listener.user);
    }

    @Test
    void doesNotReemitThresholdExceededWhenAdditionalLoginAttemptsAreMade() {
        for (int i = 0; i < SecurityConstants.MAX_LOGIN_ATTEMPTS * 2; i++) {
            loginTracker.onLoginFailure(new LoginFailureEvent(user));
        }
        verify(listener, times(1)).onEvent(ArgumentMatchers.any());
    }

    @Test
    void successfulLoginResetsCounter() {
        for (int i = 0; i < SecurityConstants.MAX_LOGIN_ATTEMPTS - 1; i++) {
            loginTracker.onLoginFailure(new LoginFailureEvent(user));
        }
        loginTracker.onLoginSuccess(new LoginSuccessEvent(user));
        for (int i = 0; i < SecurityConstants.MAX_LOGIN_ATTEMPTS; i++) {
            loginTracker.onLoginFailure(new LoginFailureEvent(user));
        }
        verify(listener, never()).onEvent(ArgumentMatchers.any());
    }

    public static class LoginListener {

        private UserAccount user;

        @EventListener
        public void onEvent(LoginAttemptsThresholdExceeded event) {
            this.user = event.getUser();
        }
    }
}