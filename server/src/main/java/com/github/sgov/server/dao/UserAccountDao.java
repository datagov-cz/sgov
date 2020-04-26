package com.github.sgov.server.dao;

import com.github.sgov.server.config.conf.PersistenceConf;
import com.github.sgov.server.exception.PersistenceException;
import com.github.sgov.server.model.UserAccount;
import com.github.sgov.server.model.util.DescriptorFactory;
import com.github.sgov.server.util.Vocabulary;
import cz.cvut.kbss.jopa.exceptions.NoResultException;
import cz.cvut.kbss.jopa.model.EntityManager;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class UserAccountDao extends BaseDao<UserAccount> {

  private final PersistenceConf config;

  @Autowired
  public UserAccountDao(EntityManager em, PersistenceConf config) {
    super(UserAccount.class, em);
    this.config = config;
  }

  @Override
  public void persist(UserAccount entity) {
    Objects.requireNonNull(entity);
    try {
      em.persist(entity, DescriptorFactory.userManagementDescriptor(entity));
    } catch (RuntimeException e) {
      throw new PersistenceException(e);
    }
  }

  /**
   * Finds a user with the specified username.
   *
   * @param username Username to search by
   * @return User with matching username
   */
  public Optional<UserAccount> findByUsername(String username) {
    Objects.requireNonNull(username);
    try {
      return Optional
          .of(em
              .createNativeQuery("SELECT ?x WHERE { ?x a ?type ; ?hasUsername ?username . }", type)
              .setParameter("type", typeUri)
              .setParameter("hasUsername", URI.create(Vocabulary.s_p_ma_uzivatelske_jmeno))
              .setParameter("username", username, config.getLanguage()).getSingleResult());
    } catch (NoResultException e) {
      return Optional.empty();
    } catch (RuntimeException e) {
      throw new PersistenceException(e);
    }
  }

  /**
   * Checks whether a user with the specified username exists.
   *
   * @param username Username to check
   * @return {@code true} if a user with the specified username exists
   */
  public boolean exists(String username) {
    Objects.requireNonNull(username);
    return em
        .createNativeQuery("ASK WHERE { ?x a ?type ; ?hasUsername ?username . }", Boolean.class)
        .setParameter("type", typeUri)
        .setParameter("hasUsername", URI.create(Vocabulary.s_p_ma_uzivatelske_jmeno))
        .setParameter("username", username, config.getLanguage()).getSingleResult();
  }

  @Override
  public List<UserAccount> findAll() {
    try {
      return em.createNativeQuery("SELECT ?x WHERE {"
          + "?x a ?type ;"
          + "?hasLastName ?lastName ;"
          + "?hasFirstName ?firstName ."
          + "} ORDER BY ?lastName ?firstName", type)
          .setParameter("type", typeUri)
          .setParameter("hasLastName", URI.create(Vocabulary.s_p_ma_prijmeni))
          .setParameter("hasFirstName", URI.create(Vocabulary.s_p_ma_krestni_jmeno))
          .getResultList();
    } catch (RuntimeException e) {
      throw new PersistenceException(e);
    }
  }
}
