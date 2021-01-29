package com.github.sgov.server.environment.config;

import com.github.sgov.server.config.conf.JwtConf;
import com.github.sgov.server.service.security.SecurityUtils;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

/**
 * This configuration class is necessary when testing security of REST controllers (e.g., {@link
 * com.github.sgov.server.controller.WorkspaceController}).
 */
@Configuration
@EnableWebSecurity
@ContextConfiguration(classes = {JwtConf.class, SecurityUtils.class})
@EnableGlobalMethodSecurity(prePostEnabled = true)
@ActiveProfiles("test")
public class TestRestSecurityConfig extends WebSecurityConfigurerAdapter {

    @Mock
    private AuthenticationProvider authenticationProvider;

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
            .exceptionHandling()
            .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            .and().cors().and().csrf().disable()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
