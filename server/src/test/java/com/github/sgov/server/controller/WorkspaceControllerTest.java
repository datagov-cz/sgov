package com.github.sgov.server.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import com.github.sgov.server.exception.NotFoundException;
import com.github.sgov.server.service.IdentifierResolver;
import com.github.sgov.server.service.WorkspaceService;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.topbraid.shacl.validation.ValidationReport;

class WorkspaceControllerTest extends BaseControllerTestRunner {

    private final URI workspaceUri = URI.create("https://example.org/test");
    @InjectMocks
    private WorkspaceController sut;
    @Mock
    private WorkspaceService workspaceService;
    @Mock
    private ValidationReport report;
    @Mock
    private IdentifierResolver resolver;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        when(resolver.resolveIdentifier("http://example.org/", "test"))
            .thenReturn(workspaceUri);
        super.setUp(sut);
    }

    @Test
    void getAllRetrievesAllWorkspaces() throws Exception {
        List<String> workspaces =
            Arrays.asList("http://example.org/test1", "http://example.org/test2");

        BDDMockito.given(workspaceService.getAllWorkspaceIris())
            .willReturn(workspaces);

        mockMvc.perform(get("/workspaces/iris")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0]", is("http://example.org/test1")))
            .andExpect(jsonPath("$[1]", is("http://example.org/test2")));
    }

    @Test
    void validateWithIriSucceeds() throws Exception {
        BDDMockito.given(workspaceService.validate(any()))
            .willReturn(report);

        mockMvc.perform(get("/workspaces/test/validate")
            .param("namespace", "http://example.org/")
            .header("Accept-language", "cs"))
            .andExpect(status().isOk());
    }

    @Test
    void validateWithNonExistingIriReturns404() throws Exception {
        BDDMockito.given(workspaceService.validate(workspaceUri))
            .willThrow(new NotFoundException(""));

        mockMvc.perform(get("/workspaces/test/validate")
            .param("namespace", "http://example.org/")
            .header("Accept-language", "cs"))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void publishWithNonExistingIriReturns404() throws Exception {
        BDDMockito.given(workspaceService.publish(workspaceUri))
            .willThrow(new NotFoundException(""));

        mockMvc.perform(post("/workspaces/test/publish")
            .param("namespace", "http://example.org/"))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void publishWithExistingIriSuceeds() throws Exception {
        BDDMockito.given(workspaceService.publish(workspaceUri))
            .willReturn(workspaceUri);

        mockMvc.perform(post("/workspaces/test/publish")
            .param("namespace", "http://example.org/"))
            .andExpect(status().isCreated());
    }
}