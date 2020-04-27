package com.github.sgov.server.controller;

import com.github.sgov.server.dao.WorkspaceDao;
import com.github.sgov.server.service.IdentifierResolver;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.topbraid.shacl.validation.ValidationReport;

@RestController
@RequestMapping("/workspace")
@Api(tags = "Workspace")
@SuppressWarnings("checkstyle:MissingJavadocType")
public class WorkspaceController extends BaseController {

  private final WorkspaceDao workspaceDao;

  @Autowired
  public WorkspaceController(WorkspaceDao workspaceDao, IdentifierResolver idResolver) {
    super(idResolver);
    this.workspaceDao = workspaceDao;
  }

  @GetMapping(value = "/all", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Retrieves IRIs of all workspaces.")
  @ResponseBody
  public List<String> getAll() {
    return workspaceDao.getAllWorkspaceIris();
  }

  @GetMapping(value = "/validate", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Validates workspace using predefined rules. This involves "
      + "e.g. checking that each term has a skos:prefLabel, or that each Role-typed "
      + "term has a super term typed as Kind.")
  @ResponseBody
  @ApiImplicitParam(name = "Accept-language",
      value = "cs",
      required = true,
      paramType = "header",
      dataTypeClass = String.class,
      example = "cs"
  )
  public ValidationReport validate(
      @ApiParam(value = "http://example.org/mc",
          required = true,
          example = "http://example.org/mc"
      ) @RequestParam String iri
  ) {
    return workspaceDao.validateWorkspace(iri);
  }
}
