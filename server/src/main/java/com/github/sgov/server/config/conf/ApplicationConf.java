package com.github.sgov.server.config.conf;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("application")
@SuppressWarnings("checkstyle:MissingJavadocType")
public class ApplicationConf {

    /**
     * Application version.
     */
    private String version;
}
