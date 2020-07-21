package com.github.sgov.server.config;

import cz.cvut.kbss.jopa.sessions.UnitOfWorkImpl;
import java.util.Arrays;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.Contact;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.service.SecurityScheme;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger.web.SecurityConfigurationBuilder;

@Configuration
@SuppressWarnings("checkstyle:MissingJavadocType")
public class SpringFoxConfig {

    /**
     * Returns Swagger Docket.
     */
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
            .pathMapping("/")
            .select()
            .apis(RequestHandlerSelectors.basePackage("com.github.sgov"))
            .paths(PathSelectors.any())
            .build()
            .enableUrlTemplating(true)
            .genericModelSubstitutes(ResponseEntity.class)
            .ignoredParameterTypes(UnitOfWorkImpl.class)
            .apiInfo(apiInfo())
            .securityContexts(Arrays.asList(securityContext()))
            .securitySchemes(Arrays.asList(securityScheme()));
    }

    @Bean
    public SecurityScheme securityScheme() {
        return new ApiKey("Bearer", "Authorization", "header");
    }

    @Bean
    SecurityConfiguration security() {
        return SecurityConfigurationBuilder.builder()
            .scopeSeparator(",")
            .additionalQueryStringParams(null)
            .useBasicAuthenticationWithAccessCodeGrant(false)
            .build();
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder().securityReferences(defaultAuth())
            .operationSelector((each) -> true).build();
    }

    private List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope(
            "global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        return Arrays.asList(new SecurityReference("Bearer",
            authorizationScopes));
    }

    ApiInfo apiInfo() {
        return new ApiInfoBuilder()
            .title("SGoV Server")
            .description("Server for Semantic Government Vocabulary (SGoV) management.")
            .version("1.0.0")
            .contact(new Contact("Petr Křemen", "", "petr.kremen@mvcr.cz"))
            .build();
    }
}
