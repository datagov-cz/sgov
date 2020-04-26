package com.github.sgov.server.service.repository;

import com.github.sgov.server.dao.GenericDao;
import com.github.sgov.server.dao.UserAccountDao;
import com.github.sgov.server.exception.NotFoundException;
import com.github.sgov.server.model.UserAccount;
import com.github.sgov.server.service.IdentifierResolver;
import javax.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

  /**
   * Checks whether a user with the specified username exists.
   *
   * @param username Username to search by
   * @return {@code true} if a user with the specifier username exists
   */
  public boolean exists(String username) {
    return userAccountDao.exists(username);
  }

  @Override
  protected UserAccount postLoad(UserAccount instance) {
    instance.erasePassword();
    return instance;
  }

  @Override
  protected void prePersist(UserAccount instance) {
    validate(instance);
    if (instance.getUri() == null) {
      instance.setUri(idResolver
          .generateUserIdentifier(instance.getFirstName(), instance.getLastName()));
    }
    instance.setPassword(passwordEncoder.encode(instance.getPassword()));
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
}
