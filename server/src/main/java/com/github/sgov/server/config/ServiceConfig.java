package com.github.sgov.server.config;

import com.github.sgov.server.config.conf.UserConf;
import com.github.sgov.server.service.Services;
import com.github.sgov.server.service.SystemInitializer;
import com.github.sgov.server.service.repository.UserRepositoryService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
@ComponentScan(basePackageClasses = Services.class)
@SuppressWarnings("checkstyle:MissingJavadocType")
public class ServiceConfig {

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * Provides JSR 380 validator for bean validation.
   */
  @Bean
  public LocalValidatorFactoryBean validatorFactoryBean() {
    return new LocalValidatorFactoryBean();
  }

  @Bean
  public SystemInitializer systemInitializer(UserConf config,
                                             UserRepositoryService userService,
                                             PlatformTransactionManager txManager) {
    return new SystemInitializer(config, userService, txManager);
  }
}

