package com.github.sgov.server.service;

import com.github.sgov.server.exception.NotFoundException;
import com.github.sgov.server.model.UserAccount;
import com.github.sgov.server.model.Workspace;
import com.github.sgov.server.service.repository.UserRepositoryService;
import com.github.sgov.server.service.repository.WorkspaceRepositoryService;
import com.github.sgov.server.service.security.SecurityUtils;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * User account-related business logic.
 */
@Service
public class UserService {

    private final UserRepositoryService repositoryService;
    private final WorkspaceRepositoryService workspaceRepositoryService;
    private final SecurityUtils securityUtils;

    /**
     * Constructor.
     */
    @Autowired
    public UserService(UserRepositoryService repositoryService,
                       WorkspaceRepositoryService workspaceRepositoryService,
                       SecurityUtils securityUtils) {
        this.repositoryService = repositoryService;
        this.workspaceRepositoryService = workspaceRepositoryService;
        this.securityUtils = securityUtils;
    }

    /**
     * Gets accounts of all users in the system.
     *
     * @return List of user accounts ordered by last name and first name
     */
    public List<UserAccount> findAll() {
        return repositoryService.findAll();
    }

    /**
     * Retrieves currently logged in user.
     *
     * @return Currently logged in user's account
     */
    public UserAccount getCurrent() {
        final UserAccount account = securityUtils.getCurrentUser();
        account.erasePassword();
        return account;
    }

    /**
     * Returns workspace of authenticated user.
     */
    public Workspace getCurrentWorkspace() {
        UserAccount uc = getCurrent();
        return Optional.ofNullable(uc.getCurrentWorkspace())
            .orElseThrow(() -> new NotFoundException(
                "Current workspace of user " + uc + " not found."));
    }

    /**
     * Set workspace of authenticated user.
     *
     * @param newWorkspaceId Workspace that should be set.
     */
    public void changeCurrentWorkspace(URI newWorkspaceId) {
        Workspace newWorkspace = workspaceRepositoryService.findRequired(newWorkspaceId);
        UserAccount uc = getCurrent();
        uc.setCurrentWorkspace(newWorkspace);
        repositoryService.update(uc);
    }

    /**
     * Remove currently set workspace of authenticated user.
     */
    public void removeCurrentWorkspace() {
        UserAccount uc = getCurrent();
        if (uc.getCurrentWorkspace() == null) {
            throw new NotFoundException("Current workspace of user " + uc + " not found.");
        }
        uc.setCurrentWorkspace(null);
        repositoryService.update(uc);
    }
}
