package com.github.sgov.server.service.repository;

import com.github.sgov.server.dao.GenericDao;
import com.github.sgov.server.dao.UserAccountDao;
import com.github.sgov.server.dao.WorkspaceDao;
import com.github.sgov.server.exception.NotFoundException;
import com.github.sgov.server.model.UserAccount;
import com.github.sgov.server.service.IdentifierResolver;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import javax.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service to managed user accounts.
 */
@Service
public class UserRepositoryService extends BaseRepositoryService<UserAccount> {

    private final UserAccountDao userAccountDao;

    private final IdentifierResolver idResolver;

    private final PasswordEncoder passwordEncoder;

    /**
     * Service for user management.
     */
    @Autowired
    public UserRepositoryService(UserAccountDao userAccountDao,
                                 IdentifierResolver idResolver,
                                 PasswordEncoder passwordEncoder,
                                 Validator validator) {
        super(validator);
        this.userAccountDao = userAccountDao;
        this.idResolver = idResolver;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    protected GenericDao<UserAccount> getPrimaryDao() {
        return userAccountDao;
    }

    @Override
    protected UserAccount postLoad(UserAccount instance) {
        instance.erasePassword();
        return instance;
    }

    @Override
    protected void preUpdate(UserAccount instance) {
        final UserAccount original = userAccountDao.find(instance.getUri()).orElseThrow(
            () -> new NotFoundException("User " + instance + " does not exist."));
        if (instance.getPassword() != null) {
            instance.setPassword(passwordEncoder.encode(instance.getPassword()));
        } else {
            instance.setPassword(original.getPassword());
        }
        validate(instance);
    }

    /**
     * Finds all user accounts.
     *
     * @return list of user accounts
     */
    public List<UserAccount> findAll() {
        final List<UserAccount> accounts = userAccountDao.findAll();
        accounts.forEach(UserAccount::erasePassword);
        return accounts;
    }

    /**
     * Finds a user account.
     *
     * @param uri user IRI
     * @return
     */
    public Optional<UserAccount> find(URI uri) {
        return userAccountDao.find(uri);
    }

    /**
     * Finds a user account. Throws NotFoundException if the user cannot be found.
     *
     * @param uri user IRI
     * @return User account
     */
    public UserAccount findRequired(URI uri) {
        return find(uri).map(u -> {
            u.erasePassword();
            return u;
        }).orElseThrow(() -> NotFoundException.create(UserAccount.class.getSimpleName(), uri));
    }
}
