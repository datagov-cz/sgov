package com.github.sgov.server.service.business;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

import com.github.sgov.server.environment.Generator;
import com.github.sgov.server.model.VocabularyContext;
import com.github.sgov.server.model.Workspace;
import com.github.sgov.server.service.BaseServiceTestRunner;
import com.github.sgov.server.service.WorkspaceService;
import com.github.sgov.server.service.repository.GithubRepositoryService;
import com.github.sgov.server.service.repository.VocabularyService;
import com.github.sgov.server.service.repository.WorkspaceRepositoryService;
import java.net.URI;
import java.text.MessageFormat;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(initializers = ConfigFileApplicationContextInitializer.class,
    classes = {WorkspaceService.class})
public class WorkspaceServiceTest extends BaseServiceTestRunner {

    @InjectMocks
    private WorkspaceService sut;

    @Mock
    private WorkspaceRepositoryService repositoryService;

    @Mock
    private VocabularyService vocabularyService;

    @Mock
    private GithubRepositoryService githubService;

    @BeforeEach
    void setUp() {
        System.out.println("Initializing mocks");
    }

    @Test
    public void checkNoReadOnlyContextsArePublished() {
        MockitoAnnotations.initMocks(this);
        System.out.println("Running");
        final Workspace workspace = Generator.generateWorkspace();
        final VocabularyContext c1 = new VocabularyContext();
        c1.setReadonly(false);
        c1.setBasedOnVocabularyVersion(URI.create("https://slovník.gov.cz/generický/a"));
        final VocabularyContext c2 = new VocabularyContext();
        c2.setBasedOnVocabularyVersion(URI.create("https://slovník.gov.cz/generický/b"));
        c2.setReadonly(true);
        workspace.addRefersToVocabularyContexts(c1);
        workspace.addRefersToVocabularyContexts(c2);
        System.out.println(repositoryService);
        Mockito.when(repositoryService.findRequired(any())).thenReturn(workspace);
        Mockito.when(githubService.createOrUpdatePullRequestToMaster(any(),any(),any())).thenReturn("");

        sut.publish(null);

        Mockito.verify(vocabularyService, times(1)).storeContext(any(),any());
        Mockito.verify(githubService, times(1)).commit(any(),any());
    }
}