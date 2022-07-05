package com.github.sgov.server.service;

import static com.github.sgov.server.service.WorkspaceUtils.attachmentStub;
import static com.github.sgov.server.service.WorkspaceUtils.stub;

import com.github.sgov.server.controller.dto.VocabularyContextDto;
import com.github.sgov.server.exception.NotFoundException;
import com.github.sgov.server.exception.ValidationException;
import com.github.sgov.server.model.AttachmentContext;
import com.github.sgov.server.model.ChangeTrackingContext;
import com.github.sgov.server.model.VocabularyContext;
import com.github.sgov.server.model.Workspace;
import com.github.sgov.server.service.repository.VocabularyRepositoryService;
import com.github.sgov.server.service.repository.WorkspaceRepositoryService;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.topbraid.shacl.validation.ValidationReport;

/**
 * Workspace-related business logic.
 */
@Service
@Slf4j
public class WorkspaceService {

    private final WorkspaceRepositoryService repositoryService;

    private final VocabularyRepositoryService vocabularyService;

    /**
     * Constructor.
     */
    @Autowired
    public WorkspaceService(WorkspaceRepositoryService repositoryService,
                            VocabularyRepositoryService vocabularyService) {
        this.repositoryService = repositoryService;
        this.vocabularyService = vocabularyService;
    }

    /**
     * Validates the workspace with the given IRI.
     *
     * @param workspaceUri Workspace that should be created.
     */
    public ValidationReport validate(URI workspaceUri) {
        final Workspace workspace = getWorkspace(workspaceUri);
        return repositoryService.validateWorkspace(workspace);
    }

    /**
     * Validates set of vocabularies identified by their context IRIs.
     *
     * @param vocabularyContextUris Set of vocabulary context IRIs.
     */
    public ValidationReport validate(Set<URI> vocabularyContextUris) {
        return repositoryService.validateVocabularies(
            vocabularyContextUris.stream().map(
                vocabularyService::findRequired
            ).collect(Collectors.toSet())
        );
    }

    private Workspace getWorkspace(URI workspaceUri) {
        final Workspace workspace = repositoryService.findRequired(workspaceUri);
        if (workspace == null) {
            throw new NotFoundException("Vocabulary context " + workspaceUri + " does not exist.");
        }
        return workspace;
    }

    public Workspace persist(Workspace instance) {
        repositoryService.persist(instance);
        return instance;
    }

    public Workspace findInferred(URI id) {
        return repositoryService.findInferred(id);
    }

    /**
     * Updates only direct attributes of the workspace.
     *
     * @param workspace Workspace that holds updated attributes.
     */
    public void update(Workspace workspace) {
        Workspace update = repositoryService.findRequired(workspace.getUri());
        update.setLabel(workspace.getLabel());
        repositoryService.update(update);
    }

    public void remove(URI id) {
        repositoryService.remove(id);
    }

    public Workspace getRequiredReference(URI id) {
        return repositoryService.getRequiredReference(id);
    }

    /**
     * Ensures that a vocabulary with the given IRI is registered in the given workspace.
     * The vocabulary is registered by first of the following actions that matches:
     * 1) if vocabulary is already in workspace return it intact
     * 2) if vocabulary exists, add it to workspace and load its content
     * 3) create new vocabulary in the workspace (unless there is same vocabulary in other
     * workspace or label of the vocabulary is not provided)
     *
     * @param workspaceUri         URI of the workspace to connect the vocabulary context to.
     * @param vocabularyContextDto vocabulary metadata
     * @param checkNotInGivenWorkspace If true pre-check that vocabulary is not already present
     *                                 in the workspace
     * @param checkNotInOtherWorkspaces If true pre-check that vocabulary is not present in
     *                                  any other workspace beside the given one
     * @param checkNotPublished If true pre-check that vocabulary is not published.
     * @return URI of the vocabulary context to create
     */
    public URI ensureVocabularyExistsInWorkspace(
        final URI workspaceUri,
        final VocabularyContextDto vocabularyContextDto,
        final boolean checkNotInGivenWorkspace,
        final boolean checkNotInOtherWorkspaces,
        final boolean checkNotPublished) {
        final URI vocabularyUri = vocabularyContextDto.getBasedOnVersion();
        final Workspace workspace = repositoryService.findRequired(workspaceUri);
        URI vocabularyContextUri =
            repositoryService.getVocabularyContextReference(workspace, vocabularyUri);
        if (vocabularyContextUri != null) {
            if (checkNotInGivenWorkspace) {
                throw new ValidationException(String.format(
                    "Unable to add %s to workspace %s."
                        + "It is already present in the workspace within context %s.",
                    vocabularyUri,
                    workspace.getUri(),
                    vocabularyContextUri));
            }
            return vocabularyContextUri;
        }

        if (vocabularyService.getVocabulariesAsContextDtos().stream()
            .noneMatch(vc ->
                vc.getBasedOnVersion().equals(vocabularyUri)
            )
        ) {
            if (vocabularyContextDto.getLabel() == null) {
                throw NotFoundException.create("Vocabulary", vocabularyUri);
            }
            if (checkNotInOtherWorkspaces) {
                vocabularyService.verifyVocabularyNotInAnyWorkspace(vocabularyUri);
            }
            return createVocabularyContext(workspace, vocabularyContextDto);
        } else {
            if (checkNotPublished) {
                throw new ValidationException(String.format(
                    "Unable to add %s to workspace %s."
                        + "The vocabulary is already published.",
                    vocabularyUri,
                    workspace.getUri()));
            }
            return loadVocabularyContextFromCache(workspace, vocabularyUri);
        }
    }

    private URI createVocabularyContext(Workspace workspace,
                                        VocabularyContextDto vocabularyContextDto) {
        URI vocabularyUri = vocabularyContextDto.getBasedOnVersion();
        URI vocabularyContextUri;
        VocabularyContext vocabularyContext = stub(vocabularyUri);
        vocabularyService.createContext(vocabularyContext, vocabularyContextDto);
        vocabularyService.persist(vocabularyContext);
        workspace.addRefersToVocabularyContexts(vocabularyContext);
        repositoryService.update(workspace);
        vocabularyContextUri =
            repositoryService.getVocabularyContextReference(workspace, vocabularyUri);
        return vocabularyContextUri;
    }

    private URI loadVocabularyContextFromCache(final Workspace workspace, final URI vocabularyUri) {
        log.info("Loading vocabulary context {} from cache in workspace {}",
            vocabularyUri, workspace.getUri());
        final VocabularyContext vocabularyContext = stub(vocabularyUri);
        workspace.addRefersToVocabularyContexts(vocabularyContext);
        repositoryService.update(workspace);
        final URI vocabularyContextUri = vocabularyContext.getUri();
        vocabularyService.loadContext(vocabularyContext);
        log.info("Found attachments {}", vocabularyContext.getAttachments());
        vocabularyContext.getAttachments().forEach(attachmentUri -> {
            log.info("Adding attachment {}", attachmentUri);
            final AttachmentContext attachmentContext = attachmentStub(attachmentUri);
            vocabularyContext.addAttachmentContext(attachmentContext);
            vocabularyService.update(vocabularyContext);
            vocabularyService.loadContext(attachmentContext);
        });
        return vocabularyContextUri;
    }

    /**
     * Collects workspaces which have the given vocabulary attached in R/W mode.
     *
     * @param vocabularyIri IRI of the vocabulary to be checked
     * @return list of workspaces
     */
    public Collection<Workspace> getWorkspacesWithReadWriteVocabulary(final URI vocabularyIri) {
        return repositoryService.findAll().stream()
            .filter(ws -> ws.getVocabularyContexts().stream()
                .anyMatch(vc -> vc.getBasedOnVersion().equals(vocabularyIri))
            ).collect(Collectors.toList());
    }

    public List<Workspace> findAllInferred() {
        return repositoryService.findAllInferred();
    }

    /**
     * Removes vocabulary context from given workspace.
     *
     * @param workspaceId         Uri of a workspace.
     * @param vocabularyFragment  String of a vocabulary context UUID.
     */
    public VocabularyContext removeVocabulary(URI workspaceId, String vocabularyFragment) {
        Workspace workspace = repositoryService.findRequired(workspaceId);
        VocabularyContext vocabularyContext = workspace.getVocabularyContexts().stream()
            .filter(vc -> vc.getUri().toString().endsWith(vocabularyFragment))
            .findFirst().orElseThrow(
                () -> NotFoundException.create(
                    VocabularyContext.class.getSimpleName(), vocabularyFragment
                )
            );
        ChangeTrackingContext changeTrackingContext = vocabularyContext.getChangeTrackingContext();
        repositoryService.clearVocabularyContext(changeTrackingContext.getUri());
        repositoryService.clearVocabularyContext(vocabularyContext.getUri());

        vocabularyService.remove(vocabularyContext);

        workspace.getVocabularyContexts().remove(vocabularyContext);
        repositoryService.update(workspace);

        return vocabularyContext;
    }

    /**
     * Retrieves all direct dependent vocabularies for the given vocabulary in the given workspace.
     *
     * @param workspaceId  Uri of a workspace.
     * @param vocabularyId Uri of a vocabulary context.
     */
    public List<URI> getDependentsForVocabularyInWorkspace(URI workspaceId, URI vocabularyId) {
        final Workspace workspace = repositoryService.findRequired(workspaceId);
        return repositoryService.getDependentsForVocabularyInWorkspace(workspace, vocabularyId);
    }
}
