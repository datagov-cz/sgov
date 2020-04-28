package com.github.sgov.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
@SuppressWarnings("checkstyle:MissingJavadocType")
public class SpringFoxConfig {

    /**
     * Returns Swagger_2 Docket.
     */
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
            .pathMapping("/")
            .select()
            .apis(RequestHandlerSelectors.basePackage("com.github.sgov"))
            .paths(PathSelectors.any())
            .build()
            .apiInfo(apiInfo());
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
