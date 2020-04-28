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
@ConfigurationProperties("user")
@SuppressWarnings("checkstyle:MissingJavadocType")
public class UserConf {

    /**
     * Specifies folder in which admin credentials are saved when his account is generated.
     *
     * @see #adminCredentialsFile
     */
    private String adminCredentialsLocation = System.getProperty("user.home");

    /**
     * Name of the file in which admin credentials are saved when his account is generated.
     *
     * <p>This file is stored in the {@link #adminCredentialsLocation}.
     *
     * @see #adminCredentialsLocation
     */
    private String adminCredentialsFile = ".sgov-admin";

    /**
     * Namespace for users.
     */
    private String namespace =
        "http://onto.fel.cvut.cz/ontologies/slovnik/agendovy/popis-dat" + "/uživatel";
}
