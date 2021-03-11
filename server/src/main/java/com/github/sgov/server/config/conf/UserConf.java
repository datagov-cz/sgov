package com.github.sgov.server.config.conf;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("user")
@SuppressWarnings("checkstyle:MissingJavadocType")
public class UserConf {

    public String userNamespace;
    public static String namespace;

    @Value("${user.namespace}")
    public void setUserNamespace(String namespace) {
        UserConf.namespace = namespace;
    }

    public String userContext;
    public static String context;

    @Value("${user.context}")
    public void setUserContext(String userContext) {
        UserConf.context = userContext;
    }

}
