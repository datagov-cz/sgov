package com.github.sgov.server.service;

import com.github.sgov.server.dao.WorkspaceDao;
import com.github.sgov.server.environment.Environment;
import com.github.sgov.server.environment.Generator;
import com.github.sgov.server.model.UserAccount;
import com.github.sgov.server.service.repository.WorkspaceRepositoryService;
import cz.cvut.kbss.jopa.model.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(initializers = ConfigFileApplicationContextInitializer.class,
    classes = {WorkspaceRepositoryService.class})
class WorkspaceServiceTest extends BaseServiceTestRunner {

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
}