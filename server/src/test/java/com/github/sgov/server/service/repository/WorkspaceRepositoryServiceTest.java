package com.github.sgov.server.service.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


import com.github.sgov.server.dao.WorkspaceDao;
import com.github.sgov.server.environment.Environment;
import com.github.sgov.server.environment.Generator;
import com.github.sgov.server.model.UserAccount;
import com.github.sgov.server.model.Workspace;
import com.github.sgov.server.model.util.DescriptorFactory;
import com.github.sgov.server.service.BaseServiceTestRunner;
import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.jopa.model.descriptors.Descriptor;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(initializers = ConfigFileApplicationContextInitializer.class,
    classes = {WorkspaceRepositoryService.class})
class WorkspaceRepositoryServiceTest extends BaseServiceTestRunner {

    @Autowired
    private EntityManager em;

    @Autowired
    private WorkspaceRepositoryService sut;

    private UserAccount user;

    @Mock
    private WorkspaceDao workspaceDao;

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
        Descriptor descriptor =
            DescriptorFactory.workspaceDescriptor(workspace);
        transactional(() -> em.persist(workspace, descriptor));

        sut.createVocabularyContext(
            workspace.getUri(),
            URI.create("http://example.org/test-vocabulary-1.0.0"),
            true);

        final Workspace result = em.find(Workspace.class, workspace.getUri(), descriptor);
        assertNotNull(result);
        assertEquals(user.toUser(), result.getAuthor());
        assertNotNull(result.getCreated());
        assertEquals(1, result.getVocabularyContexts().size());
        assertNotNull(result.getVocabularyContexts().iterator().next().getChangeTrackingContext());
    }

    //TODO
    void getAllWorkspaceIrisReturnsAllIris() {
        final List<String> iris = new ArrayList<>();
        iris.add("http://example.org");
        iris.add("http://example2.org");
        Mockito.when(workspaceDao.getAllWorkspaceIris()).thenReturn(iris);
        Assert.assertEquals(iris, sut.getAllWorkspaceIris());
    }
}