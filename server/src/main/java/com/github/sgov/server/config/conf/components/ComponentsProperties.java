package com.github.sgov.server.config.conf.components;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@EnableConfigurationProperties
@SuppressWarnings("checkstyle:MissingJavadocType")
public class ComponentsProperties {

    @Value("${components}")
    private String componentsRaw;

    private ComponentsConf components;

    @Bean
    public ComponentsConf getComponents() {
        return ComponentsConverter.convertStatic(this.componentsRaw);
    }
}
