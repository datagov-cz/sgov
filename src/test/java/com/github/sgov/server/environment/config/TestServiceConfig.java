package com.github.sgov.server.environment.config;

import com.github.sgov.server.environment.Environment;
import com.github.sgov.server.service.Services;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.client.RestTemplate;

@TestConfiguration
@ComponentScan(basePackageClasses = {Services.class})
@ContextConfiguration(classes = {UserDetailsService.class})
public class TestServiceConfig {

    /**
     * Rest template.
     */
    @Bean
    public RestTemplate restTemplate() {
        final RestTemplate client = new RestTemplate();
        final MappingJackson2HttpMessageConverter jacksonConverter =
            new MappingJackson2HttpMessageConverter();
        jacksonConverter.setObjectMapper(Environment.getObjectMapper());
        final StringHttpMessageConverter stringConverter =
            new StringHttpMessageConverter(StandardCharsets.UTF_8);
        client.setMessageConverters(
            Arrays.asList(jacksonConverter, stringConverter, new ResourceHttpMessageConverter()));
        return client;
    }

    @Bean
    public LocalValidatorFactoryBean validatorFactoryBean() {
        return new LocalValidatorFactoryBean();
    }

    @Bean
    public ClassPathResource languageSpecification() {
        return new ClassPathResource("languages/language.ttl");
    }
}
