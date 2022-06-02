package com.github.sgov.server.controller;

import com.github.sgov.server.service.WorkspaceService;
import com.github.sgov.server.util.Constants;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.topbraid.shacl.validation.ValidationReport;

@RestController
public class ValidationController {

    private final WorkspaceService workspaceService;

    @Autowired
    public ValidationController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    /**
     * Validates set of vocabularies specified by their context IRIs.
     *
     * @param vocabularyContextIris context IRIs of vocabularies to be validated.
     * @return set of validation results
     */
    @GetMapping(value = "/validate",
        produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Validates set of vocabularies using predefined rules. This involves "
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
    @PreAuthorize("permitAll()")
    public ValidationReport validate(
        @RequestParam(name = Constants.QueryParams.VOCABULARY_CONTEXT_IRI)
            List<String> vocabularyContextIris
    ) {
        final Set<URI> vcIris = vocabularyContextIris.stream()
            .map(URI::create).collect(Collectors.toSet());
        return workspaceService.validate(vcIris);
    }
}
