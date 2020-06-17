package com.github.sgov.server.service;


import com.github.sgov.server.exception.NotFoundException;
import com.github.sgov.server.model.VocabularyContext;
import com.github.sgov.server.model.Workspace;
import com.github.sgov.server.service.repository.UserRepositoryService;
import com.github.sgov.server.service.repository.WorkspaceRepositoryService;
import com.github.sgov.server.service.security.SecurityUtils;
import java.net.URI;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.topbraid.shacl.validation.ValidationReport;

/**
 * Workspace-related business logic.
 */
@Service
public class WorkspaceService {

    private final WorkspaceRepositoryService repositoryService;
    private final UserRepositoryService userRepositoryService;
    private final UserService userService;
    private final SecurityUtils securityUtils;

    /**
     * Constructor.
     */
    @Autowired
    public WorkspaceService(WorkspaceRepositoryService repositoryService,
                            UserRepositoryService userRepositoryService,
                            UserService userService,
                            SecurityUtils securityUtils) {
        this.repositoryService = repositoryService;
        this.userRepositoryService = userRepositoryService;
        this.userService = userService;
        this.securityUtils = securityUtils;
    }


    public List<String> getAllWorkspaceIris() {
        return repositoryService.getAllWorkspaceIris();
    }

    public ValidationReport validateWorkspace(String workspaceIri) {
        return repositoryService.validateWorkspace(workspaceIri);
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
     * Ensures that a vocabulary with the given IRI is registered in the workspace. If yes, its
     * content is kept intact. Otherwise a new context is created and the content is loaded from
     *
     * @param workspaceUri  URI of the workspace to connect the vocabulary context to.
     * @param vocabularyUri URI of the vocabulary to be attached to the workspace
     * @param isReadOnly    true if the context should be created as read-only (no effect if the
     *                      vocabulary already exists)
     * @return URI of the vocabulary context to create
     */
    public URI ensureVocabularyExistsInWorkspace(
        URI workspaceUri, URI vocabularyUri, boolean isReadOnly) {
        URI vocabularyContextUri =
            repositoryService.getVocabularyContextReference(workspaceUri, vocabularyUri);
        if (vocabularyContextUri == null) {
            VocabularyContext vocabularyContext =
                repositoryService.createVocabularyContext(workspaceUri, vocabularyUri, isReadOnly);
            repositoryService.loadContext(vocabularyContext);
            vocabularyContextUri = vocabularyContext.getUri();
        }

        return vocabularyContextUri;
    }

    public List<Workspace> findAllInferred() {
        return repositoryService.findAllInferred();
    }

    /**
     * Removes vocabulary context from given workspace.
     * @param workspaceId Uri of a workspace.
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
        workspace.getVocabularyContexts().remove(vocabularyContext);
        repositoryService.update(workspace);
        return vocabularyContext;
    }
}
