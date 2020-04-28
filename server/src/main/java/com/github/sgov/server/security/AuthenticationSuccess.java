package com.github.sgov.server.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sgov.server.security.model.LoginStatus;
import java.io.IOException;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Service;

/**
 * Writes basic login/logout information into the response.
 */
@Service
public class AuthenticationSuccess implements AuthenticationSuccessHandler, LogoutSuccessHandler {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationSuccess.class);

    private final ObjectMapper mapper;

    @Autowired
    public AuthenticationSuccess(@Qualifier("objectMapper") ObjectMapper mapper) {
        this.mapper = mapper;
    }

    private static String getUsername(Authentication authentication) {
        return authentication != null ? ((UserDetails) authentication.getDetails()).getUsername()
            : "";
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest httpServletRequest,
                                        HttpServletResponse httpServletResponse,
                                        Authentication authentication) throws IOException {
        Objects.requireNonNull(authentication);
        final String username = getUsername(authentication);
        final LoginStatus loginStatus = new LoginStatus()
            .setLoggedIn(true)
            .setSuccess(authentication.isAuthenticated())
            .setUsername(username)
            .setErrorMessage(null);
        mapper.writeValue(httpServletResponse.getOutputStream(), loginStatus);
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest httpServletRequest,
                                HttpServletResponse httpServletResponse,
                                Authentication authentication) throws IOException {
        LOG.trace("Successfully logged out user {}", getUsername(authentication));


        final LoginStatus loginStatus = new LoginStatus()
            .setLoggedIn(false)
            .setSuccess(true)
            .setUsername(null)
            .setErrorMessage(null);
        mapper.writeValue(httpServletResponse.getOutputStream(), loginStatus);
    }
}
