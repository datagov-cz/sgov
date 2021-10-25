package com.github.sgov.server.provenance;

import com.github.sgov.server.model.HasProvenanceData;
import com.github.sgov.server.model.UserAccount;
import com.github.sgov.server.service.security.SecurityUtils;
import cz.cvut.kbss.jopa.model.annotations.PostLoad;
import cz.cvut.kbss.jopa.model.annotations.PrePersist;
import cz.cvut.kbss.jopa.model.annotations.PreUpdate;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;


/**
 * Entity listener used to manage provenance data.
 */
@Slf4j
public class ProvenanceManager {

    private UserAccount getCurrent() {
        return SecurityUtils.getCurrentUser();
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
            log.trace("Removing provenance data of instance {} for anonymous access.", instance);
            instance.setAuthor(null);
            instance.setLastEditor(null);
        }
    }
}
