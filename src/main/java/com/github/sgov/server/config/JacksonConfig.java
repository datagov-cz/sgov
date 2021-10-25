package com.github.sgov.server.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.sgov.server.controller.util.ValidationReportSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.topbraid.shacl.validation.ValidationReport;

@Configuration
@SuppressWarnings("checkstyle:MissingJavadocType")
public class JacksonConfig {

    /**
     * Generates an object mapper. It is made RequestScope in order to use Accept-language inside
     * the serializer.
     *
     * @return Object mapper for serializing ValidationReport
     */
    @Bean
    public ObjectMapper objectMapper() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        final SimpleModule module = new SimpleModule();
        module.addSerializer(ValidationReport.class, new ValidationReportSerializer());
        mapper.registerModule(module);
        return mapper;
    }
}
