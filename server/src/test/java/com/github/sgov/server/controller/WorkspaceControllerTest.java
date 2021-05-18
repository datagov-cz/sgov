package com.github.sgov.server.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import com.github.sgov.server.exception.NotFoundException;
import com.github.sgov.server.model.Workspace;
import com.github.sgov.server.service.WorkspaceService;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.topbraid.shacl.validation.ValidationReport;
import org.topbraid.shacl.validation.ValidationResult;

class WorkspaceControllerTest extends BaseControllerTestRunner {

    private final URI workspaceUri = URI.create("https://example.org/test");
    @InjectMocks
    private WorkspaceController sut;
    @Mock
    private WorkspaceService workspaceService;

    private ValidationReport report;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        super.setUp(sut);
        report = new ValidationReport() {
            @Override public boolean conforms() {
                return true;
            }

            @Override public List<ValidationResult> results() {
                return Collections.emptyList();
            }
        };
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    void getAllRetrievesAllWorkspaces() throws Exception {
        final Workspace ws1 = new Workspace();
        ws1.setUri(URI.create("http://example.org/test1"));
        final Workspace ws2 = new Workspace();
        ws2.setUri(URI.create("http://example.org/test2"));
        final List<Workspace> workspaces = Arrays.asList(ws1, ws2);

        BDDMockito.given(workspaceService.findAllInferred())
            .willReturn(workspaces);

        mockMvc.perform(get("/workspaces")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].uri", is("http://example.org/test1")))
            .andExpect(jsonPath("$[1].uri", is("http://example.org/test2")));
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
            .param("namespace", "https://example.org/")
            .header("Accept-language", "cs"))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void publishWithNonExistingIriReturns404() throws Exception {
        BDDMockito.given(workspaceService.publish(workspaceUri))
            .willThrow(new NotFoundException(""));

        mockMvc.perform(post("/workspaces/test/publish")
            .param("namespace", "https://example.org/"))
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

    @Test
    void getDependenciesRetrievesAllDependencies() throws Exception {
        final URI v1 = URI.create("https://example.org/1");
        final URI v2 = URI.create("https://example.org/2");

        BDDMockito.given(workspaceService.getDependentsForVocabularyInWorkspace(workspaceUri, v1))
            .willReturn(Collections.singletonList(v2));

        mockMvc.perform(get("/workspaces/test/dependencies")
            .param("vocabularyIri", "https://example.org/1")
            .param("namespace", "https://example.org/")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));
    }
}