package com.github.sgov.server.dao;

import com.github.sgov.server.exception.PersistenceException;
import com.github.sgov.server.model.VocabularyContext;
import com.github.sgov.server.model.util.DescriptorFactory;
import cz.cvut.kbss.jopa.model.EntityManager;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * DAO for accessing workspace.
 */
@Slf4j
@Repository
public class VocabularyDao extends BaseDao<VocabularyContext> {

    @Autowired
    public VocabularyDao(EntityManager em) {
        super(VocabularyContext.class, em);
    }

    @Override
    public VocabularyContext update(VocabularyContext entity) {
        Objects.requireNonNull(entity);
        try {
            // Evict possibly cached instance loaded from default context
            em.getEntityManagerFactory()
                .getCache().evict(VocabularyContext.class, entity.getUri(), null);
            return em.merge(entity, DescriptorFactory.vocabularyDescriptor(entity.getUri()));
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }
}
