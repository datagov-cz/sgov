package com.github.sgov.server.provenance;

import com.github.sgov.server.SGoVServiceApplication;
import com.github.sgov.server.model.HasProvenanceData;
import com.github.sgov.server.model.UserAccount;
import com.github.sgov.server.service.security.SecurityUtils;
import cz.cvut.kbss.jopa.model.annotations.PostLoad;
import cz.cvut.kbss.jopa.model.annotations.PrePersist;
import cz.cvut.kbss.jopa.model.annotations.PreUpdate;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Entity listener used to manage provenance data.
 */
public class ProvenanceManager {

    private static final Logger LOG = LoggerFactory.getLogger(ProvenanceManager.class);

    private UserAccount getCurrent() {
        final SecurityUtils securityUtils = SGoVServiceApplication.context
            .getBean(SecurityUtils.class);
        return securityUtils.getCurrentUser();
    }

    /**
     * Sets provenance data (author, datetime of creation) of the specified instance.
     *
     * @param instance Instance being persisted for which provenance data will be generated
     */
    @PrePersist
    void generateOnPersist(HasProvenanceData instance) {
        assert instance != null;
        assert getCurrent() != null;

        instance.setAuthor(getCurrent().toUser());
        instance.setCreated(new Date());
        generateOnUpdate(instance);
    }

    @PreUpdate
    void generateOnUpdate(HasProvenanceData instance) {
        assert instance != null;
        assert getCurrent() != null;

        instance.setLastEditor(getCurrent().toUser());
        instance.setLastModified(new Date());
    }

    /**
     * Clears author data after instance load in case of anonymous access, i.e. , when no user is
     * authenticated.
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
