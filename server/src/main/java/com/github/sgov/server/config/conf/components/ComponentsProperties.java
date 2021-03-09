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
//@ComponentScan(basePackageClasses = ComponentsConverter.class)
@SuppressWarnings("checkstyle:MissingJavadocType")
public class ComponentsProperties {
    // @Value("#{T(com.github.sgov.server.config.conf.components.ComponentsConverter)
    // .convertStatic('${components}')}\"")
    // private ComponentsConf componentsParsed;
    @Value("${components}")
    private String componentsRaw;

    private ComponentsConf components;

    @Bean
    public ComponentsConf getComponents() {
        return ComponentsConverter.convertStatic(this.componentsRaw);
    }
}
