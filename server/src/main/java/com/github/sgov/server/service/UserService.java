package com.github.sgov.server.service;

import com.github.sgov.server.controller.dto.UserUpdateDto;
import com.github.sgov.server.event.LoginAttemptsThresholdExceeded;
import com.github.sgov.server.exception.AuthorizationException;
import com.github.sgov.server.exception.NotFoundException;
import com.github.sgov.server.exception.ValidationException;
import com.github.sgov.server.model.UserAccount;
import com.github.sgov.server.service.repository.UserRepositoryService;
import com.github.sgov.server.service.security.SecurityUtils;
import com.github.sgov.server.util.Vocabulary;
import java.net.URI;
import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * User account-related business logic.
 */
@Service
public class UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    private final UserRepositoryService repositoryService;

    private final SecurityUtils securityUtils;

    @Autowired
    public UserService(UserRepositoryService repositoryService, SecurityUtils securityUtils) {
        this.repositoryService = repositoryService;
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
     * Finds a user with the specified id.
     *
     * @param id User identifier
     * @return Matching user wrapped in an {@code Optional}
     */
    public Optional<UserAccount> find(URI id) {
        return repositoryService.find(id);
    }

    /**
     * Finds a user with the specified id.
     *
     * @param id User identifier
     * @return Matching user account
     * @throws NotFoundException When no matching account is found
     */
    public UserAccount findRequired(URI id) {
        return repositoryService.findRequired(id);
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
     * Persists the specified user account.
     *
     * @param account Account to save
     */
    @Transactional
    public void persist(UserAccount account) {
        Objects.requireNonNull(account);
        LOG.trace("Persisting user account {}.", account);
        if (this.exists(account.getUsername())) {
            throw new ValidationException(
                MessageFormat.format("User with username {0} already exists.",
                    account.getUsername()));
        }
        if (!securityUtils.isAuthenticated() || !securityUtils.getCurrentUser().isAdmin()) {
            account.addType(Vocabulary.s_c_omezeny_uzivatel);
            account.removeType(Vocabulary.s_c_administrator);
        }
        repositoryService.persist(account);
    }

    /**
     * Updates current user's account with the specified update data.
     *
     * <p>If the update contains also password update, the original password specified in the
     * update object has to match current user's password.
     *
     * @param update Account update data
     * @throws AuthorizationException If the update data concern other than the current user
     */
    @Transactional
    public void updateCurrent(UserUpdateDto update) {
        LOG.trace("Updating current user account.");
        Objects.requireNonNull(update);
        final UserAccount currentUser = securityUtils.getCurrentUser();

        if (!currentUser.getUri().equals(update.getUri())) {
            throw new AuthorizationException(
                MessageFormat.format("User {0} attempted to update a different user''s account.",
                    currentUser));
        }
        if (!currentUser.getUsername().equals(update.getUsername())) {
            throw new ValidationException(
                MessageFormat.format("User {0} attempted to update his username.",
                    currentUser));
        }
        if (update.getPassword() != null) {
            securityUtils.verifyCurrentUserPassword(update.getOriginalPassword());
        }
        repositoryService.update(update.asUserAccount());
    }

    /**
     * Unlocks the specified user account.
     *
     * <p>The specified password is set as the new password of the user account.
     *
     * @param account     Account to unlock
     * @param newPassword New password for the unlocked account
     */
    @Transactional
    public void unlock(UserAccount account, String newPassword) {
        Objects.requireNonNull(account);
        Objects.requireNonNull(newPassword);
        ensureNotOwnAccount(account, "unlock");
        LOG.trace("Unlocking user account {}.", account);
        account.unlock();
        account.setPassword(newPassword);
        repositoryService.update(account);
    }

    private void ensureNotOwnAccount(UserAccount account, String operation) {
        if (securityUtils.getCurrentUser().equals(account)) {
            throw new UnsupportedOperationException("Cannot " + operation + " your own account!");
        }
    }

    /**
     * Disables the specified user account.
     *
     * @param account Account to disable
     */
    @Transactional
    public void disable(UserAccount account) {
        Objects.requireNonNull(account);
        ensureNotOwnAccount(account, "disable");
        LOG.trace("Disabling user account {}.", account);
        account.disable();
        repositoryService.update(account);
    }

    /**
     * Enables the specified user account.
     *
     * @param account Account to enable
     */
    @Transactional
    public void enable(UserAccount account) {
        Objects.requireNonNull(account);
        ensureNotOwnAccount(account, "enable");
        LOG.trace("Enabling user account {}.", account);
        account.enable();
        repositoryService.update(account);
    }

    /**
     * Locks user account when unsuccessful login attempts limit is exceeded.
     *
     * <p>This is an application event listener and should not be called directly.
     *
     * @param event The event emitted when login attempts limit is exceeded
     */
    @Transactional
    @EventListener
    public void onLoginAttemptsThresholdExceeded(LoginAttemptsThresholdExceeded event) {
        Objects.requireNonNull(event);
        final UserAccount account = event.getUser();
        LOG.trace("Locking user account {} due to exceeding unsuccessful login attempts limit.",
            account);
        account.lock();
        repositoryService.update(account);
    }

    /**
     * Checks whether a user account with the specified username exists in the repository.
     *
     * @param username Username to check
     * @return Whether username already exists
     */
    public boolean exists(String username) {
        return repositoryService.exists(username);
    }
}
