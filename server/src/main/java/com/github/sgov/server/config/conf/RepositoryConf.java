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
@ConfigurationProperties("repository")
@SuppressWarnings("checkstyle:MissingJavadocType")
public class RepositoryConf {

    /**
     * URL of the release SPARQL endpoint.
     */
    private String releaseSparqlEndpointUrl;

    /**
     * URL of the workspace repository.
     */
    private String url;

    private String username;

    private String password;
}
