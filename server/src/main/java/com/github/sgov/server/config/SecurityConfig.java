package com.github.sgov.server.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sgov.server.security.AuthenticationSuccess;
import com.github.sgov.server.security.JwtAuthenticationFilter;
import com.github.sgov.server.security.JwtAuthorizationFilter;
import com.github.sgov.server.security.JwtUtils;
import com.github.sgov.server.security.Security;
import com.github.sgov.server.security.SecurityConstants;
import com.github.sgov.server.service.security.SGoVUserDetailsService;
import com.github.sgov.server.service.security.SecurityUtils;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@ComponentScan(basePackageClasses = Security.class)
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@SuppressWarnings({
    "checkstyle:MultipleStringLiterals",
    "checkstyle:ClassFanOutComplexity",
    "checkstyle:MissingJavadocType"
})
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final AuthenticationProvider authenticationProvider;

    private final AuthenticationEntryPoint authenticationEntryPoint;

    private final AuthenticationSuccess authenticationSuccessHandler;

    private final AuthenticationFailureHandler authenticationFailureHandler;

    private final JwtUtils jwtUtils;

    private final SecurityUtils securityUtils;

    private final SGoVUserDetailsService userDetailsService;

    private final ObjectMapper objectMapper;

    /**
     * SecurityConfig.
     */
    @Autowired
    @SuppressWarnings("checkstyle:ParameterNumber")
    public SecurityConfig(AuthenticationProvider authenticationProvider,
                          AuthenticationEntryPoint authenticationEntryPoint,
                          AuthenticationSuccess authenticationSuccessHandler,
                          AuthenticationFailureHandler authenticationFailureHandler,
                          JwtUtils jwtUtils, SecurityUtils securityUtils,
                          SGoVUserDetailsService userDetailsService,
                          ObjectMapper objectMapper) {
        this.authenticationProvider = authenticationProvider;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.authenticationSuccessHandler = authenticationSuccessHandler;
        this.authenticationFailureHandler = authenticationFailureHandler;
        this.jwtUtils = jwtUtils;
        this.securityUtils = securityUtils;
        this.userDetailsService = userDetailsService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(authenticationProvider);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests().antMatchers("/rest/query").permitAll().and().cors().and().csrf()
            .disable()
            .authorizeRequests().antMatchers("/**").permitAll()
            .and().exceptionHandling().authenticationEntryPoint(authenticationEntryPoint)
            .and().cors().and().csrf().disable()
            .addFilter(authenticationFilter())
            .addFilter(
                new JwtAuthorizationFilter(authenticationManager(), jwtUtils, securityUtils,
                    userDetailsService,
                    objectMapper))
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    /**
     * JwtAuthenticationFilter.
     */
    @Bean
    public JwtAuthenticationFilter authenticationFilter() throws Exception {
        final JwtAuthenticationFilter authenticationFilter =
            new JwtAuthenticationFilter(authenticationManager(),
                jwtUtils);
        authenticationFilter.setFilterProcessesUrl(SecurityConstants.SECURITY_CHECK_URI);
        authenticationFilter.setAuthenticationSuccessHandler(authenticationSuccessHandler);
        authenticationFilter.setAuthenticationFailureHandler(authenticationFailureHandler);
        return authenticationFilter;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        // We're allowing all methods from all origins so that the application API is usable also by
        // other clients
        // than just the UI.
        // This behavior can be restricted later.
        final CorsConfiguration corsConfiguration =
            new CorsConfiguration().applyPermitDefaultValues();
        corsConfiguration.setAllowedMethods(Collections.singletonList("*"));
        corsConfiguration.setAllowedOrigins(Collections.singletonList("*"));
        corsConfiguration.addExposedHeader(HttpHeaders.AUTHORIZATION);
        corsConfiguration.addExposedHeader(HttpHeaders.LOCATION);
        corsConfiguration.addExposedHeader(HttpHeaders.CONTENT_DISPOSITION);
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }
}
