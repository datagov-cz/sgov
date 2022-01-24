package com.github.sgov.server.service;

import static com.github.sgov.server.service.WorkspaceUtils.attachmentStub;
import static com.github.sgov.server.service.WorkspaceUtils.stub;

import com.github.sgov.server.controller.dto.VocabularyContextDto;
import com.github.sgov.server.exception.NotFoundException;
import com.github.sgov.server.model.AttachmentContext;
import com.github.sgov.server.model.ChangeTrackingContext;
import com.github.sgov.server.model.VocabularyContext;
import com.github.sgov.server.model.Workspace;
import com.github.sgov.server.service.repository.VocabularyRepositoryService;
import com.github.sgov.server.service.repository.WorkspaceRepositoryService;
import java.net.URI;
import java.util.Collection;
import java.util.List;
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
     * Ensures that a vocabulary with the given IRI is registered in the workspace. - If the
     * vocabulary does not exist, an error is thrown. - if the vocabulary exists and is part of the
     * workspace, nothing happens, and the content is left intact. - if the vocabulary exists, is
     * NOT part of the workspace, and should be added as R/W it is only added to the workspace if no
     * other workspace is registering the vocabulary in R/W. - if the vocabulary exists and is NOT
     * part of the workspace, it is added to the workspace and its content is loaded.
     *
     * @param workspaceUri  URI of the workspace to connect the vocabulary context to.
     * @param vocabularyContextDto vocabulary metadata
     * @return URI of the vocabulary context to create
     */
    public URI ensureVocabularyExistsInWorkspace(
        final URI workspaceUri, final VocabularyContextDto vocabularyContextDto) {
        final URI vocabularyUri = vocabularyContextDto.getBasedOnVersion();
        final Workspace workspace = repositoryService.findRequired(workspaceUri);
        URI vocabularyContextUri =
            repositoryService.getVocabularyContextReference(workspace, vocabularyUri);
        if (vocabularyContextUri != null) {
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
            return createVocabularyContext(workspace, vocabularyContextDto);
        } else {
            return loadVocabularyContextFromCache(workspace, vocabularyUri);
        }
    }

    private URI createVocabularyContext(Workspace workspace,
                                        VocabularyContextDto vocabularyContextDto) {
        URI vocabularyUri = vocabularyContextDto.getBasedOnVersion();
        URI vocabularyContextUri;
        VocabularyContext vocabularyContext = stub(vocabularyUri);
        workspace.addRefersToVocabularyContexts(vocabularyContext);
        repositoryService.update(workspace);
        vocabularyContextUri =
            repositoryService.getVocabularyContextReference(workspace, vocabularyUri);
        vocabularyContext = vocabularyService.findRequired(vocabularyContextUri);
        vocabularyService.createContext(vocabularyContext, vocabularyContextDto);
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
            workspace.addAttachmentContext(attachmentContext);
            repositoryService.update(workspace);
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
     * @param vocabularyContextId Uri of a vocabulary context.
     */
    public VocabularyContext removeVocabulary(URI workspaceId, URI vocabularyContextId) {
        Workspace workspace = repositoryService.findRequired(workspaceId);
        VocabularyContext vocabularyContext = workspace.getVocabularyContexts().stream()
            .filter(vc -> vc.getUri().equals(vocabularyContextId))
            .findFirst().orElseThrow(
                () -> NotFoundException.create(
                    VocabularyContext.class.getSimpleName(), vocabularyContextId
                )
            );
        ChangeTrackingContext changeTrackingContext = vocabularyContext.getChangeTrackingContext();
        repositoryService.clearVocabularyContext(changeTrackingContext.getUri());
        repositoryService.clearVocabularyContext(vocabularyContextId);

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
