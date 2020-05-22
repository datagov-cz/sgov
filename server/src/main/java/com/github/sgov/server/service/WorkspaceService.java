package com.github.sgov.server.service;


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

    public VocabularyContext createVocabularyContext(
            URI workspaceUri, URI vocabularyUri, boolean isReadOnly) {
        return repositoryService.createVocabularyContext(workspaceUri, vocabularyUri, isReadOnly);
    }

    public List<Workspace> findAll() {
        return repositoryService.findAll();
    }
}
