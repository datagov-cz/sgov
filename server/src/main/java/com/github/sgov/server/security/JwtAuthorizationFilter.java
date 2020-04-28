package com.github.sgov.server.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sgov.server.controller.dto.ErrorInfo;
import com.github.sgov.server.exception.JwtException;
import com.github.sgov.server.security.model.SGoVUserDetails;
import com.github.sgov.server.service.security.SGoVUserDetailsService;
import com.github.sgov.server.service.security.SecurityUtils;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

/**
 * Filter authorizing based on JWT token.
 */
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private final JwtUtils jwtUtils;

    private final SecurityUtils securityUtils;

    private final SGoVUserDetailsService userDetailsService;

    private final ObjectMapper objectMapper;

    /**
     * This filter retrieves JWT from the incoming request and validates it, ensuring that the user
     * is authorized to access the application.
     */
    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, JwtUtils jwtUtils,
                                  SecurityUtils securityUtils,
                                  SGoVUserDetailsService userDetailsService,
                                  ObjectMapper objectMapper) {
        super(authenticationManager);
        this.jwtUtils = jwtUtils;
        this.securityUtils = securityUtils;
        this.userDetailsService = userDetailsService;
        this.objectMapper = objectMapper;
    }

    @Override
    @SuppressWarnings("checkstyle:ReturnCount")
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain)
        throws IOException, ServletException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith(SecurityConstants.JWT_TOKEN_PREFIX)) {
            chain.doFilter(request, response);
            return;
        }
        final String authToken = authHeader.substring(SecurityConstants.JWT_TOKEN_PREFIX.length());
        try {
            final SGoVUserDetails userDetails = jwtUtils.extractUserInfo(authToken);
            final SGoVUserDetails existingDetails =
                userDetailsService.loadUserByUsername(userDetails.getUsername());
            SecurityUtils.verifyAccountStatus(existingDetails.getUser());
            securityUtils.setCurrentUser(existingDetails);
            refreshToken(authToken, response);
        } catch (DisabledException | LockedException | JwtException | UsernameNotFoundException e) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            objectMapper.writeValue(response.getOutputStream(),
                ErrorInfo.createWithMessage(e.getMessage(), request.getRequestURI()));
            return;
        }
        chain.doFilter(request, response);
    }

    private void refreshToken(String authToken, HttpServletResponse response) {
        final String newToken = jwtUtils.refreshToken(authToken);
        response
            .setHeader(HttpHeaders.AUTHORIZATION, SecurityConstants.JWT_TOKEN_PREFIX + newToken);
    }
}
