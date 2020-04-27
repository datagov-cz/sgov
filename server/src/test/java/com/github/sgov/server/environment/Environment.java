package com.github.sgov.server.environment;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.sgov.server.model.UserAccount;
import com.github.sgov.server.security.model.AuthenticationToken;
import com.github.sgov.server.security.model.SGoVUserDetails;
import cz.cvut.kbss.jsonld.ConfigParam;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.jackson.JsonLdModule;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

public class Environment {

  public static final String BASE_URI = "https://example.org/ex/";

  private static UserAccount currentUser;

  private static ObjectMapper objectMapper;

  private static ObjectMapper jsonLdObjectMapper;

  /**
   * Gets current user as security principal.
   *
   * @return Current user authentication as principal or {@code null} if there is no current user
   */
  public static Optional<Principal> getCurrentUserPrincipal() {
    return SecurityContextHolder.getContext() != null
        ? Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication()) :
        Optional.empty();
  }

  public static UserAccount getCurrentUser() {
    return currentUser;
  }

  /**
   * Initializes security context with the specified user.
   *
   * @param user User to set as currently authenticated
   */
  public static void setCurrentUser(UserAccount user) {
    currentUser = user;
    final SGoVUserDetails userDetails = new SGoVUserDetails(user, new HashSet<>());
    SecurityContext context = new SecurityContextImpl();
    context
        .setAuthentication(new AuthenticationToken(userDetails.getAuthorities(), userDetails));
    SecurityContextHolder.setContext(context);
  }

  /**
   * Gets a Jackson {@link ObjectMapper} for mapping JSON to Java and vice versa.
   *
   * @return {@code ObjectMapper}
   */
  public static ObjectMapper getObjectMapper() {
    if (objectMapper == null) {
      objectMapper = new ObjectMapper();
      objectMapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
      objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
      objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      // JSR 310 (Java 8 DateTime API)
      objectMapper.registerModule(new JavaTimeModule());
    }
    return objectMapper;
  }

  /**
   * Gets a Jackson {@link ObjectMapper} for mapping JSON-LD to Java and vice versa.
   *
   * @return {@code ObjectMapper}
   */
  public static ObjectMapper getJsonLdObjectMapper() {
    if (jsonLdObjectMapper == null) {
      jsonLdObjectMapper = new ObjectMapper();
      jsonLdObjectMapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
      jsonLdObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
      jsonLdObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      final JsonLdModule module = new JsonLdModule();
      module.configure(ConfigParam.SCAN_PACKAGE, "cz.cvut.kbss.termit");
      jsonLdObjectMapper.registerModule(module);
    }
    return jsonLdObjectMapper;
  }

  /**
   * Creates a Jackson JSON-LD message converter.
   *
   * @return JSON-LD message converter
   */
  public static HttpMessageConverter<?> createJsonLdMessageConverter() {
    final MappingJackson2HttpMessageConverter converter =
        new MappingJackson2HttpMessageConverter(
            getJsonLdObjectMapper());
    converter
        .setSupportedMediaTypes(
            Collections.singletonList(MediaType.valueOf(JsonLd.MEDIA_TYPE)));
    return converter;
  }

  public static HttpMessageConverter<?> createDefaultMessageConverter() {
    return new MappingJackson2HttpMessageConverter(getObjectMapper());
  }

  public static HttpMessageConverter<?> createStringEncodingMessageConverter() {
    return new StringHttpMessageConverter(StandardCharsets.UTF_8);
  }

  public static HttpMessageConverter<?> createResourceMessageConverter() {
    return new ResourceHttpMessageConverter();
  }
}
