package com.github.sgov.server.service;


import com.github.sgov.server.model.VocabularyContext;
import com.github.sgov.server.model.Workspace;
import com.github.sgov.server.service.repository.UserRepositoryService;
import com.github.sgov.server.service.repository.WorkspaceRepositoryService;
import com.github.sgov.server.service.security.SecurityUtils;
import java.net.URI;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.topbraid.shacl.validation.ValidationReport;

/**
 * Workspace-related business logic.
 */
@Service
public class WorkspaceService {
    private static final Logger LOG = LoggerFactory.getLogger(WorkspaceService.class);

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

    public Workspace findRequired(URI id) {
        return repositoryService.findRequired(id);
    }

    /**
     * Updates only direct attributes of the workspace.
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
            LOG.debug("Creating vocabulary context for vocabulary {}", vocabularyUri);
            final VocabularyContext vocabularyContext =
                repositoryService.createVocabularyContext(workspaceUri, vocabularyUri, isReadOnly);
            LOG.debug(" - done, with uri {}", vocabularyContext.getUri());

            LOG.debug("Loading vocabulary context {}", vocabularyContext.getUri());
            repositoryService.loadContext(vocabularyContext.getUri(),vocabularyUri);
            LOG.debug(" - done.");

            vocabularyContextUri = vocabularyContext.getUri();
        }

        return vocabularyContextUri;
    }

    public List<Workspace> findAll() {
        return repositoryService.findAll();
    }
}
