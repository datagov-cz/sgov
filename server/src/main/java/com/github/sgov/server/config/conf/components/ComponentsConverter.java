package com.github.sgov.server.config.conf.components;

import java.util.Base64;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

@Component
@ConfigurationPropertiesBinding
public class ComponentsConverter implements Converter<String, ComponentsConf> {

    @Override
    public ComponentsConf convert(String from) {
        return convertStatic(from);
    }

    /**
     * Converts a base64 string into ComponentsConf properties.
     *
     * @param from base64 representation fo the COMPONENTS variable
     * @return parsed configuration
     */
    public static ComponentsConf convertStatic(String from) {
        final String componentsDecoded = new String(Base64.getDecoder().decode(from));
        return new ComponentsConf(new Yaml(new ComponentsConstructor()).load(componentsDecoded));
    }
}