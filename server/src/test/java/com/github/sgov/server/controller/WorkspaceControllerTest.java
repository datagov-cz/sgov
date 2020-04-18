package com.github.sgov.server.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.sgov.server.data.WorkspaceDao;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.topbraid.shacl.validation.ValidationReport;

@ExtendWith(SpringExtension.class)
@WebMvcTest(WorkspaceController.class)
class WorkspaceControllerTest {

  @Autowired
  private MockMvc mvc;

  @MockBean
  private WorkspaceDao workspaceDao;

  @Mock
  private ValidationReport report;

  @Test
  void getAllRetrievesAllWorkspaces() throws Exception {
    List<String> workspaces = Arrays.asList("http://example.org/test1", "http://example.org/test2");

    BDDMockito.given(workspaceDao.getAllWorkspaceIris())
        .willReturn(workspaces);

    mvc.perform(get("/workspace/all")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0]", is("http://example.org/test1")))
        .andExpect(jsonPath("$[1]", is("http://example.org/test2")));
  }

  @Test
  void validateWithoutIriThrows400() throws Exception {
    BDDMockito.given(workspaceDao.validateWorkspace(anyString()))
        .willReturn(report);

    mvc.perform(get("/workspace/validate"))
        .andExpect(status().is4xxClientError());
  }

  @Test
  void validateWithIriSucceeds() throws Exception {
    BDDMockito.given(workspaceDao.validateWorkspace(anyString()))
        .willReturn(report);

    mvc.perform(get("/workspace/validate")
        .param("iri", "http://example.org/test")
        .header("Accept-language", "cs"))
        .andExpect(status().isOk());
  }
}