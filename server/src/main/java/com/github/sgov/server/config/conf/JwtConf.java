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
@ConfigurationProperties("jwt")
@SuppressWarnings("checkstyle:MissingJavadocType")
public class JwtConf {

    /**
     * OntoDriver class for the repository.
     */
    private String secretKey;
}
