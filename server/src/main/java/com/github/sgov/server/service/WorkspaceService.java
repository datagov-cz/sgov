package com.github.sgov.server.service;


import com.github.sgov.server.exception.NotFoundException;
import com.github.sgov.server.model.UserAccount;
import com.github.sgov.server.model.VocabularyContext;
import com.github.sgov.server.model.Workspace;
import com.github.sgov.server.service.repository.UserRepositoryService;
import com.github.sgov.server.service.repository.WorkspaceRepositoryService;
import com.github.sgov.server.service.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.topbraid.shacl.validation.ValidationReport;

import java.net.URI;
import java.util.List;
import java.util.Optional;

/**
 * Workspace-related business logic.
 */
@Service
public class WorkspaceService {

    private final WorkspaceRepositoryService repositoryService;
    private final UserRepositoryService userRepositoryService;
    private final UserService userService;
    private final SecurityUtils securityUtils;

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

    public VocabularyContext createVocabularyContext(URI workspaceUri, URI vocabularyUri, boolean isReadOnly) {
        return repositoryService.createVocabularyContext(workspaceUri, vocabularyUri, isReadOnly);
    }

    public Workspace getCurrentWorkspace() {
        UserAccount uc = userService.getCurrent();
        return Optional.ofNullable(uc.getCurrentWorkspace())
                .orElseThrow(() -> new NotFoundException("Current workspace of user " + uc + " not found."));
    }

    public void updateCurrentWorkspace(URI newWorkspaceId) {
        Workspace newWorkspace = repositoryService.findRequired(newWorkspaceId);
        UserAccount uc = userService.getCurrent();
        uc.setCurrentWorkspace(newWorkspace);
        userRepositoryService.update(uc);
    }

    public void removeCurrentWorkspace() {
        UserAccount uc = userService.getCurrent();
        if (uc.getCurrentWorkspace() == null) {
            throw new NotFoundException("Current workspace of user " + uc + " not found.");
        }
        uc.setCurrentWorkspace(null);
        userRepositoryService.update(uc);
    }

    public List<Workspace> findAll() {
        return repositoryService.findAll();
    }
}
