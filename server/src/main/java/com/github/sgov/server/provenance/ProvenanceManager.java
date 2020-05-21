package com.github.sgov.server.provenance;

import com.github.sgov.server.model.HasProvenanceData;
import com.github.sgov.server.service.security.SecurityUtils;
import cz.cvut.kbss.jopa.model.annotations.PostLoad;
import cz.cvut.kbss.jopa.model.annotations.PrePersist;
import cz.cvut.kbss.jopa.model.annotations.PreUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Entity listener used to manage provenance data.
 */
public class ProvenanceManager {

    private static final Logger LOG = LoggerFactory.getLogger(ProvenanceManager.class);

    /**
     * Sets provenance data (author, datetime of creation) of the specified instance.
     *
     * @param instance Instance being persisted for which provenance data will be generated
     */
    @PrePersist
    void generateOnPersist(HasProvenanceData instance) {
        assert instance != null;
        assert SecurityUtils.currentUser() != null;

        instance.setAuthor(SecurityUtils.currentUser().toUser());
        instance.setCreated(new Date());
    }

    @PreUpdate
    void generateOnUpdate(HasProvenanceData instance) {
        assert instance != null;
        assert SecurityUtils.currentUser() != null;

        instance.setLastEditor(SecurityUtils.currentUser().toUser());
        instance.setLastModified(new Date());
    }

    /**
     * Clears author data after instance load in case of anonymous access, i.e., when no user is authenticated.
     *
     * @param instance Loaded instance
     */
    @PostLoad
    void clearForAnonymousOnLoad(HasProvenanceData instance) {
        assert instance != null;

        if (!SecurityUtils.authenticated()) {
            LOG.trace("Removing provenance data of instance {} for anonymous access.", instance);
            instance.setAuthor(null);
            instance.setLastEditor(null);
        }
    }
}
