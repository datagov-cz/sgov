package com.github.sgov.server.config;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@Getter
@Setter
@Accessors(chain = true)
@ConfigurationProperties("backend")
public class BackendProperties {
  private String repositoryUrl;
}