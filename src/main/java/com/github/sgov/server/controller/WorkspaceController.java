package com.github.sgov.server.controller;

import com.github.sgov.server.controller.dto.VocabularyContextDto;
import com.github.sgov.server.model.VocabularyContext;
import com.github.sgov.server.model.Workspace;
import com.github.sgov.server.service.WorkspacePublicationService;
import com.github.sgov.server.service.WorkspaceService;
import com.github.sgov.server.util.Constants.QueryParams;
import com.github.sgov.server.util.Vocabulary;
import cz.cvut.kbss.jsonld.JsonLd;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.net.URI;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.topbraid.shacl.validation.ValidationReport;

@RestController
@RequestMapping("/workspaces")
@Api(tags = "Workspace")
@SuppressWarnings("checkstyle:MissingJavadocType")
@Slf4j
public class WorkspaceController extends BaseController {

    private final WorkspaceService workspaceService;

    private final WorkspacePublicationService workspacePublicationService;

    @Autowired
    public WorkspaceController(WorkspaceService workspaceService,
                               WorkspacePublicationService workspacePublicationService) {
        this.workspaceService = workspaceService;
        this.workspacePublicationService = workspacePublicationService;
    }

    @GetMapping(produces = {
        MediaType.APPLICATION_JSON_VALUE, JsonLd.MEDIA_TYPE})
    @ApiOperation(value = "Retrieve all workspaces.")
    public List<Workspace> getAllWorkspaces() {
        return workspaceService.findAllInferred();
    }

    /**
     * Create new workspace. If uri of workspace is not specified, it will be generated.
     *
     * @param workspace Workspace that should be created.
     */
    @PostMapping
    @ApiOperation(value = "Create new workspace.")
    public ResponseEntity<Void> createWorkspace(
        @RequestBody Workspace workspace) {
        Workspace ws = workspaceService.persist(workspace);
        log.debug("Workspace {} created.", ws);
        return ResponseEntity.created(
            generateLocation(ws.getUri(), Vocabulary.s_c_metadatovy_kontext)
        ).build();
    }

    /**
     * Retrieve existing workspace.
     *
     * @param workspaceFragment local name of workspace id.
     * @param namespace         Namespace used for resource identifier resolution. Optional, if not
     *                          specified, the configured namespace is used.
     * @return Workspace specified by workspaceFragment and optionally namespace.
     */
    @GetMapping(value = "/{workspaceFragment}",
        produces = {
            MediaType.APPLICATION_JSON_VALUE,
            JsonLd.MEDIA_TYPE})
    @ApiOperation(value = "Retrieve existing workspace.")
    public Workspace getWorkspace(
        @PathVariable String workspaceFragment,
        @RequestParam(name = QueryParams.NAMESPACE, required = false) String namespace) {
        final URI identifier = resolveIdentifier(
            namespace, workspaceFragment, Vocabulary.s_c_metadatovy_kontext);
        final Workspace workspace = workspaceService.findInferred(identifier);
        log.info("Workspace {} " + workspace.getLastModified().getTime() + " : "
            + workspace.getLastModifiedOrCreated().getTime(), workspace.getUri());
        return workspace;
    }

    /**
     * Update existing workspace.
     *
     * @param workspace         Workspace that will be updated.
     * @param workspaceFragment local name of workspace id.
     * @param namespace         Namespace used for resource identifier resolution. Optional, if not
     *                          specified, the configured namespace is used.
     */
    @PutMapping(value = "/{workspaceFragment}", consumes = {
        MediaType.APPLICATION_JSON_VALUE, JsonLd.MEDIA_TYPE})
    @ApiOperation(value = "Update existing workspace.")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateWorkspace(@PathVariable String workspaceFragment,
                                @RequestParam(name = QueryParams.NAMESPACE, required = false)
                                String namespace,
                                @RequestBody Workspace workspace) {
        final URI identifier = resolveIdentifier(
            namespace, workspaceFragment, Vocabulary.s_c_metadatovy_kontext);
        verifyRequestAndEntityIdentifier(workspace, identifier);
        workspaceService.update(workspace);
        log.debug("Workspace {} updated.", workspace);
    }

    /**
     * Delete existing workspace.
     *
     * @param workspaceFragment local name of workspace id.
     * @param namespace         Namespace used for resource identifier resolution. Optional, if not
     *                          specified, the configured namespace is used.
     */
    @DeleteMapping(value = "/{workspaceFragment}")
    @ApiOperation(value = "Delete existing workspace.")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWorkspace(@PathVariable String workspaceFragment,
                                @RequestParam(name = QueryParams.NAMESPACE, required = false)
                                String namespace) {
        final URI identifier = resolveIdentifier(
            namespace, workspaceFragment, Vocabulary.s_c_metadatovy_kontext);
        final Workspace toRemove = workspaceService.getRequiredReference(identifier);
        workspaceService.remove(identifier);
        log.debug("Workspace {} deleted.", toRemove);
    }

    /**
     * Retrieve all vocabulary contexts stored within workspace.
     *
     * @param workspaceFragment local name of workspace id.
     * @param namespace         Namespace used for resource identifier resolution. Optional, if not
     *                          specified, the configured namespace is used.
     * @return Set of vocabulary contexts.
     */
    @GetMapping(value = "/{workspaceFragment}/vocabularies",
        produces = {
            MediaType.APPLICATION_JSON_VALUE,
            JsonLd.MEDIA_TYPE})
    @ApiOperation(value = "Retrieve all vocabulary contexts stored within workspace.")
    public Set<VocabularyContext> getAllVocabularyContexts(
        @PathVariable String workspaceFragment,
        @RequestParam(name = QueryParams.NAMESPACE, required = false) String namespace) {
        final URI identifier = resolveIdentifier(
            namespace, workspaceFragment, Vocabulary.s_c_metadatovy_kontext);
        final Workspace workspace = workspaceService.findInferred(identifier);
        return workspace.getVocabularyContexts();
    }


    /**
     * Get vocabularies dependent on the current vocabulary in the given workspace.
     *
     * @param workspaceFragment local name of the workspace
     * @param vocabularyIri     IRI of the vocabulary.
     * @param namespace         Namespace used for resource identifier resolution. Optional, if not
     *                          specified, the configured namespace is used.
     * @return Workspace specified by workspaceFragment and optionally namespace.
     */
    @GetMapping(value = "/{workspaceFragment}/dependencies",
        produces = {
            MediaType.APPLICATION_JSON_VALUE,
            JsonLd.MEDIA_TYPE})
    @ApiOperation(value = "Get vocabularies dependent on the given vocabulary.")
    @Deprecated
    public List<URI> getDependentVocabularies(
        @PathVariable String workspaceFragment,
        @RequestParam(name = QueryParams.VOCABULARY_IRI) String vocabularyIri,
        @RequestParam(name = QueryParams.NAMESPACE, required = false) String namespace) {
        final URI workspaceId = resolveIdentifier(
            namespace, workspaceFragment, Vocabulary.s_c_metadatovy_kontext);
        final URI vocabularyId = URI.create(vocabularyIri);
        return workspaceService.getDependentsForVocabularyInWorkspace(workspaceId, vocabularyId);
    }


    /**
     * Adds a vocabulary to a workspace.
     *
     * <p>
     * This creates a new vocabulary context within a workspace and its relevant change context. If
     * the label is provided, a new vocabulary is created, otherwise an existing one is loaded.
     * </p>
     *
     * @param workspaceFragment local name of workspace id.
     * @param namespace         Namespace used for resource identifier resolution. Optional, if not
     *                          specified, the configured namespace is used.
     * @param vocabularyUri     Uri of the vocabulary for which context should be created.
     * @param label             Label of the vocabulary, if specified,
     *                          the new vocabulary is created.
     * @param ensureNotEdited   Ensure that vocabulary is not edited in other context.
     */
    @Deprecated
    @PostMapping(value = "/{workspaceFragment}/vocabularies")
    @ApiOperation(value =
        "Create vocabulary context within workspace. A new vocabulary is created "
            + "if not found and in this case label is required.")
    public ResponseEntity<String> addVocabularyToWorkspace(
        @PathVariable String workspaceFragment,
        @RequestParam(name = QueryParams.NAMESPACE, required = false) String namespace,
        @RequestParam(name = "vocabularyUri", required = false) URI vocabularyUri,
        @RequestParam(name = "label", required = false) String label,
        @RequestParam(name = "ensureNotEdited", required = false,
            defaultValue = "false") boolean ensureNotEdited
    ) {
        final URI workspaceUri = resolveIdentifier(
            namespace, workspaceFragment, Vocabulary.s_c_metadatovy_kontext);
        final boolean ensureNotPublished = label != null;

        final URI vocabularyContextUri =
            workspaceService.ensureVocabularyExistsInWorkspace(
                workspaceUri,
                new VocabularyContextDto()
                    .setBasedOnVersion(vocabularyUri)
                    .setLabel(label),
                true,
                ensureNotEdited,
                ensureNotPublished
            );
        log.debug("Vocabulary context {} added to workspace.", vocabularyContextUri);
        return ResponseEntity.created(
            generateLocation(vocabularyContextUri, Vocabulary.s_c_slovnikovy_kontext)
        ).build();
    }

    /**
     * Adds a vocabulary to a workspace.
     *
     * <p>
     * This creates a new vocabulary context within a workspace and its relevant change context. If
     * the label is provided, a new vocabulary is created, otherwise an existing one is loaded.
     * </p>
     *
     * @param workspaceFragment local name of workspace id.
     * @param namespace         Namespace used for resource identifier resolution. Optional, if not
     *                          specified, the configured namespace is used.
     * @param vocabularyContext vocabularyContext to create/load
     * @param ensureNotEdited   Ensure that vocabulary is not edited in other context.
     */
    @PostMapping(value = "/{workspaceFragment}/vocabularies-full")
    @ApiOperation(value =
        "Create vocabulary context within workspace. A new vocabulary is created "
            + "if not found and in this case label is required.")
    public ResponseEntity<String> addVocabularyToWorkspace(
        @PathVariable String workspaceFragment,
        @RequestParam(name = QueryParams.NAMESPACE, required = false) String namespace,
        @RequestParam(name = "ensureNotEdited", required = false,
            defaultValue = "false") boolean ensureNotEdited,
        @RequestBody VocabularyContextDto vocabularyContext
    ) {
        final URI workspaceUri = resolveIdentifier(
            namespace, workspaceFragment, Vocabulary.s_c_metadatovy_kontext);
        final boolean ensureNotPublished = vocabularyContext.getLabel() != null;

        final URI vocabularyContextUri =
            workspaceService.ensureVocabularyExistsInWorkspace(
                workspaceUri,
                vocabularyContext,
                true,
                ensureNotEdited,
                ensureNotPublished
            );
        log.debug("Vocabulary context {} added to workspace.", vocabularyContextUri);
        return ResponseEntity.created(
            generateLocation(vocabularyContextUri, Vocabulary.s_c_slovnikovy_kontext)
        ).build();
    }

    /**
     * Delete vocabulary from a workspace.
     *
     * @param workspaceFragment  local name of workspace id.
     * @param vocabularyId       vocabulary context URI.
     * @param namespace          Namespace used for resource identifier resolution. Optional, if not
     *                           specified, the configured namespace is used.
     */
    @DeleteMapping(value = "/{workspaceFragment}/vocabularies")
    @ApiOperation(value = "Delete vocabulary form a workspace.")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWorkspaceVocabulary(
        @PathVariable String workspaceFragment,
        @RequestBody URI vocabularyId,
        @RequestParam(name = QueryParams.NAMESPACE, required = false)
        String namespace) {
        final URI workspaceId = resolveIdentifier(
            namespace, workspaceFragment, Vocabulary.s_c_metadatovy_kontext);
        VocabularyContext toRemove =
            workspaceService.removeVocabulary(workspaceId, vocabularyId);
        log.debug("Vocabulary context {} deleted from workspace {}.", toRemove, workspaceId);
    }

    /**
     * Validates a workspace.
     *
     * @param workspaceFragment local name of workspace id.
     * @param namespace         Namespace used for resource identifier resolution. Optional, if not
     *                          specified, the configured namespace is used.
     * @return set of validation results
     */
    @GetMapping(value = "/{workspaceFragment}/validate",
        produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
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
    @PreAuthorize("permitAll()")
    public ValidationReport validate(
        @ApiParam(value = "instance-1775747014",
            required = true,
            example = "instance-1775747014"
        )
        @PathVariable String workspaceFragment,
        @ApiParam(
            value = "https://slovník.gov.cz/datový/pracovní-prostor/pojem/metadatový-kontext/",
            example = "https://slovník.gov.cz/datový/pracovní-prostor/pojem/metadatový-kontext/"
        )
        @RequestParam(name = QueryParams.NAMESPACE, required = false) String namespace
    ) {
        final URI identifier = resolveIdentifier(
            namespace, workspaceFragment, Vocabulary.s_c_metadatovy_kontext);
        return workspaceService.validate(identifier);
    }

    /**
     * Publishes a workspace.
     *
     * @param workspaceFragment local name of workspace id.
     * @param namespace         Namespace used for resource identifier resolution. Optional, if not
     *                          specified, the configured namespace is used.
     */
    @PostMapping(value = "/{workspaceFragment}/publish",
        produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Publishes workspace to GitHub.")
    public ResponseEntity<String> publish(
        @ApiParam(value = "instance-1775747014",
            required = true,
            example = "instance-1775747014"
        )
        @PathVariable String workspaceFragment,
        @ApiParam(
            value = "https://slovník.gov.cz/datový/pracovní-prostor/pojem/metadatový-kontext/",
            example = "https://slovník.gov.cz/datový/pracovní-prostor/pojem/metadatový-kontext/"
        )
        @RequestParam(name = QueryParams.NAMESPACE, required = false) String namespace
    ) {
        final URI identifier = resolveIdentifier(
            namespace, workspaceFragment, Vocabulary.s_c_metadatovy_kontext);
        URI id = workspacePublicationService.publish(identifier);
        log.info("Workspace published at {}", id);
        return ResponseEntity.created(
            id
        ).build();
    }
}
