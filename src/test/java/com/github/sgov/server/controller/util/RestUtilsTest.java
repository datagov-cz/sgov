package com.github.sgov.server.controller.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.sgov.server.environment.Generator;
import com.github.sgov.server.security.SecurityConstants;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import javax.servlet.http.Cookie;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class RestUtilsTest {

    @Test
    void createLocationHeaderFromCurrentUriWithPathAddsPathWithVariableReplacementsToRequestUri() {
        final MockHttpServletRequest mockRequest =
            new MockHttpServletRequest(HttpMethod.GET.toString(),
                "/vocabularies");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequest));
        final String id = "117";

        final URI result = RestUtils.createLocationFromCurrentUriWithPath("/{id}", id);
        assertThat(result.toString(), endsWith("/vocabularies/" + id));
    }

    @Test
    void createLocationHeaderFromCurrentUriWithPathAndQueryCreatesLocationHeader() {
        final MockHttpServletRequest mockRequest =
            new MockHttpServletRequest(HttpMethod.GET.toString(),
                "/vocabularies");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequest));
        final String name = "metropolitan-plan";
        final String param = "namespace";
        final String paramValue = "http://onto.fel.cvut.cz/ontologies/termit/vocabularies/";
        final URI result =
            RestUtils
                .createLocationFromCurrentUriWithPathAndQuery("/{name}", param, paramValue, name);
        assertThat(result.toString(), endsWith("/" + name + "?" + param + "=" + paramValue));
    }
}
