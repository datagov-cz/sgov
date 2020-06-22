package com.github.sgov.server.controller;

import static com.github.sgov.server.environment.Environment.createDefaultMessageConverter;
import static com.github.sgov.server.environment.Environment.createJsonLdMessageConverter;
import static com.github.sgov.server.environment.Environment.createResourceMessageConverter;
import static com.github.sgov.server.environment.Environment.createStringEncodingMessageConverter;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sgov.server.controller.util.RestExceptionHandler;
import com.github.sgov.server.environment.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.accept.ContentNegotiationManager;

/**
 * Common configuration for REST controller tests.
 */
public class BaseControllerTestRunner {

    ObjectMapper objectMapper;

    ObjectMapper jsonLdObjectMapper;

    MockMvc mockMvc;

    /**
     * Sets up the controller.
     */
    public void setUp(Object controller) {
        setupObjectMappers();
        this.mockMvc =
            MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new RestExceptionHandler())
                .setMessageConverters(createJsonLdMessageConverter(),
                    createDefaultMessageConverter(), createStringEncodingMessageConverter(),
                    createResourceMessageConverter())
                .setContentNegotiationManager(new ContentNegotiationManager())
                .build();
    }

    void setupObjectMappers() {
        this.objectMapper = Environment.getObjectMapper();
        this.jsonLdObjectMapper = Environment.getJsonLdObjectMapper();
    }

    String toJson(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }

    String toJsonLd(Object object) throws Exception {
        return jsonLdObjectMapper.writeValueAsString(object);
    }

    <T> T readValue(MvcResult result, Class<T> targetType) throws Exception {
        return objectMapper.readValue(result.getResponse().getContentAsByteArray(), targetType);
    }

    <T> T readValue(MvcResult result, TypeReference<T> targetType) throws Exception {
        return objectMapper.readValue(result.getResponse().getContentAsByteArray(), targetType);
    }

    void verifyLocationEquals(String expectedPath, MvcResult result) {
        final String locationHeader = result.getResponse().getHeader(HttpHeaders.LOCATION);
        assertNotNull(locationHeader);
        final String path = locationHeader.substring(0,
            locationHeader.indexOf('?') != -1 ? locationHeader.indexOf('?') :
                locationHeader.length());
        assertEquals("http://localhost" + expectedPath, path);
    }
}
