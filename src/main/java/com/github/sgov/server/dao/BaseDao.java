package com.github.sgov.server.dao;

import com.github.sgov.server.exception.PersistenceException;
import com.github.sgov.server.model.util.EntityToOwlClassMapper;
import com.github.sgov.server.model.util.HasIdentifier;
import cz.cvut.kbss.jopa.model.EntityManager;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Base implementation of the generic DAO API.
 *
 * @param <T> the entity class this DAO manages
 */
@SuppressWarnings("checkstyle:MultipleStringLiterals")
public abstract class BaseDao<T extends HasIdentifier> implements GenericDao<T> {

    protected final Class<T> type;
    protected final URI typeUri;

    protected final EntityManager em;

    protected BaseDao(Class<T> type, EntityManager em) {
        this.type = type;
        this.typeUri = URI.create(EntityToOwlClassMapper.getOwlClassForEntity(type));
        this.em = em;
    }

    @Override
    public List<T> findAll() {
        try {
            return em.createNativeQuery("SELECT ?x WHERE { ?x a ?type . }", type)
                .setParameter("type", typeUri)
                .getResultList();
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public Optional<T> find(URI id) {
        Objects.requireNonNull(id);
        try {
            return Optional.ofNullable(em.find(type, id));
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public Optional<T> getReference(URI id) {
        Objects.requireNonNull(id);
        try {
            return Optional.ofNullable(em.getReference(type, id));
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void persist(T entity) {
        Objects.requireNonNull(entity);
        try {
            em.persist(entity);
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public T update(T entity) {
        Objects.requireNonNull(entity);
        try {
            return em.merge(entity);
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void remove(T entity) {
        Objects.requireNonNull(entity);
        try {
            em.remove(em.merge(entity));
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void remove(URI id) {
        Objects.requireNonNull(id);
        try {
            find(id).ifPresent(em::remove);
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public boolean exists(URI id) {
        Objects.requireNonNull(id);
        try {
            return em.createNativeQuery("ASK { ?x a ?type . }", Boolean.class)
                .setParameter("x", id)
                .setParameter("type", typeUri)
                .getSingleResult();
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }
}
