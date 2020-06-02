package com.github.sgov.server.service.repository;

import com.github.sgov.server.dao.WorkspaceDao;
import com.github.sgov.server.service.BaseServiceTestRunner;
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
class WorkspaceRepositoryServiceTest  extends BaseServiceTestRunner {

    @Autowired
    private WorkspaceRepositoryService sut;

    @Mock
    private WorkspaceDao workspaceDao;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void getAllWorkspaceIrisReturnsAllIris() {
        final List<String> iris = new ArrayList<>();
        iris.add("http://example.org");
        iris.add("http://example2.org");
        Mockito.when(workspaceDao.getAllWorkspaceIris()).thenReturn(iris);
        Assert.assertEquals(iris, sut.getAllWorkspaceIris());
    }
}

