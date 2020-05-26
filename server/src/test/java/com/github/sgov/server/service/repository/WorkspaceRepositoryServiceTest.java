package com.github.sgov.server.service.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


import com.github.sgov.server.environment.Environment;
import com.github.sgov.server.environment.Generator;
import com.github.sgov.server.model.UserAccount;
import com.github.sgov.server.model.Workspace;
import com.github.sgov.server.service.BaseServiceTestRunner;
import cz.cvut.kbss.jopa.model.EntityManager;
import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

class WorkspaceRepositoryServiceTest extends BaseServiceTestRunner {

    @Autowired
    private EntityManager em;

    @Autowired
    private WorkspaceRepositoryService sut;

    private UserAccount user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        this.user = Generator.generateUserAccountWithPassword();
        transactional(() -> em.persist(user));
        Environment.setCurrentUser(user);
    }

    @Test
    void createVocabularyContextUpdatesWorkspace() {
        final Workspace workspace = Generator.generateWorkspace();
        transactional(() -> em.persist(workspace));

        sut.createVocabularyContext(
            workspace.getUri(),
            URI.create("http://example.org/test-vocabulary-1.0.0"),
            true);

        final Workspace result = em.find(Workspace.class, workspace.getUri());
        assertNotNull(result);
        assertEquals(user.toUser(), result.getAuthor());
        assertNotNull(result.getCreated());
        assertEquals(1, result.getVocabularyContexts().size());
        assertNotNull(result.getVocabularyContexts().iterator().next().getChangeTrackingContext());
    }
}
