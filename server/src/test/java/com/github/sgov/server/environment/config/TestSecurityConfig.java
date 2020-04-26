package com.github.sgov.server.environment.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sgov.server.environment.Environment;
import com.github.sgov.server.security.Security;
import javax.servlet.http.HttpServletRequest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.mock.web.MockHttpServletRequest;

@TestConfiguration
@ComponentScan(basePackageClasses = {Security.class})
public class TestSecurityConfig {

  @Bean
  public HttpServletRequest request() {
    return new MockHttpServletRequest();
  }

  @Bean
  public ObjectMapper objectMapper() {
    return Environment.getObjectMapper();
  }

}
