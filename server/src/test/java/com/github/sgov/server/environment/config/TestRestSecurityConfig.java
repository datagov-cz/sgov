package com.github.sgov.server.environment.config;

import com.github.sgov.server.config.conf.JwtConf;
import com.github.sgov.server.environment.Environment;
import com.github.sgov.server.security.HttpAuthenticationEntryPoint;
import com.github.sgov.server.security.JwtAuthenticationFilter;
import com.github.sgov.server.security.JwtAuthorizationFilter;
import com.github.sgov.server.security.JwtUtils;
import com.github.sgov.server.service.security.SGoVUserDetailsService;
import com.github.sgov.server.service.security.SecurityUtils;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

/**
 * This configuration class is necessary when testing security of REST controllers (e.g., {@link
 * com.github.sgov.server.controller.WorkspaceController}).
 */
@Configuration
@EnableWebSecurity
@ContextConfiguration(classes = {JwtConf.class, JwtUtils.class, SecurityUtils.class})
@EnableGlobalMethodSecurity(prePostEnabled = true)
@ActiveProfiles("test")
public class TestRestSecurityConfig extends WebSecurityConfigurerAdapter {

    private AuthenticationEntryPoint authenticationEntryPoint = new HttpAuthenticationEntryPoint();

    @Mock
    private AuthenticationFailureHandler authenticationFailureHandler;

    @Mock
    private AuthenticationSuccessHandler authenticationSuccessHandler;

    @Mock
    private AuthenticationProvider authenticationProvider;

    @Mock
    private SGoVUserDetailsService userDetailsService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private SecurityUtils securityUtils;

    protected TestRestSecurityConfig() {
        MockitoAnnotations.initMocks(this);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(authenticationProvider);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests().anyRequest().permitAll().and()
            .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint)
            .and().cors().and().csrf().disable()
            .addFilter(new JwtAuthenticationFilter(authenticationManager(), jwtUtils))
            .addFilter(new JwtAuthorizationFilter(authenticationManager(), jwtUtils, securityUtils,
                userDetailsService,
                Environment.getObjectMapper()))
            .formLogin().successHandler(authenticationSuccessHandler)
            .failureHandler(authenticationFailureHandler).and().sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
