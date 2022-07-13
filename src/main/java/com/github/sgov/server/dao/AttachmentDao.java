package com.github.sgov.server.dao;

import com.github.sgov.server.exception.PersistenceException;
import com.github.sgov.server.model.AttachmentContext;
import com.github.sgov.server.model.util.DescriptorFactory;
import cz.cvut.kbss.jopa.model.EntityManager;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * DAO for accessing Attachment contexts.
 */
@Slf4j
@Repository
public class AttachmentDao extends BaseDao<AttachmentContext> {

    private final DescriptorFactory descriptorFactory;

    @Autowired
    public AttachmentDao(EntityManager em, DescriptorFactory descriptorFactory) {
        super(AttachmentContext.class, em);
        this.descriptorFactory = descriptorFactory;
    }

    @Override
    public AttachmentContext update(AttachmentContext entity) {
        Objects.requireNonNull(entity);
        try {
            // Evict possibly cached instance loaded from default context
            em.getEntityManagerFactory()
                .getCache().evict(AttachmentContext.class, entity.getUri(), null);
            return em.merge(entity, descriptorFactory.attachmentDescriptor(entity));
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void persist(AttachmentContext entity) {
        Objects.requireNonNull(entity);
        try {
            em.persist(entity, descriptorFactory.attachmentDescriptor(entity));
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }
}
