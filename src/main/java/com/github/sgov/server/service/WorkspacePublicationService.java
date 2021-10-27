package com.github.sgov.server.service;

import static com.github.sgov.server.service.WorkspaceUtils.createPullRequestBody;

import com.github.sgov.server.exception.PublicationException;
import com.github.sgov.server.model.VocabularyContext;
import com.github.sgov.server.model.Workspace;
import com.github.sgov.server.service.repository.GithubRepositoryService;
import com.github.sgov.server.service.repository.VocabularyRepositoryService;
import com.github.sgov.server.service.repository.WorkspaceRepositoryService;
import com.github.sgov.server.util.Utils;
import com.github.sgov.server.util.VocabularyFolder;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service to publish workspaces.
 */
@Service
@Slf4j
public class WorkspacePublicationService {

    private final WorkspaceRepositoryService repositoryService;

    private final GithubRepositoryService githubService;

    private final VocabularyRepositoryService vocabularyService;

    /**
     * Constructor.
     */
    @Autowired
    public WorkspacePublicationService(final GithubRepositoryService githubService,
                                       final VocabularyRepositoryService vocabularyService,
                                       final WorkspaceRepositoryService repositoryService) {
        this.githubService = githubService;
        this.vocabularyService = vocabularyService;
        this.repositoryService = repositoryService;
    }

    /**
     * Publishes the workspace with the given IRI.
     *
     * @param workspaceUri Workspace that should be published.
     * @return GitHub PR URL
     */
    public URI publish(URI workspaceUri) {
        final Workspace workspace = repositoryService.findRequired(workspaceUri);
        final String branchName = WorkspaceUtils.createBranchName(workspace);

        try {
            final File dir = java.nio.file.Files.createTempDirectory("sgov").toFile();
            try (final Git git = githubService.checkout(branchName, dir)) {
                publishVocabularyContexts(git, dir, workspace);
                githubService.push(git);
                FileUtils.deleteDirectory(dir);
                String prUrl = githubService.createOrUpdatePullRequestToMaster(branchName,
                    MessageFormat.format("Publishing workspace {0} ({1})", workspace.getLabel(),
                        workspace.getUri()),
                    createPullRequestBody(workspace));

                return URI.create(prUrl);
            }
        } catch (IOException e) {
            throw new PublicationException("An exception occurred during publishing workspace.",
                e);
        }
    }

    private void publishVocabularyContexts(Git git, File dir, Workspace workspace) {
        for (final VocabularyContext c : workspace.getVocabularyContexts()) {
            final URI iri = c.getBasedOnVersion();
            try {
                final VocabularyFolder folder = Utils.getVocabularyFolder(dir, iri.toString());
                clearVocabularyFolder(git, folder);
                vocabularyService.storeContext(c, folder);
                githubService.commit(git, MessageFormat.format(
                    "Publishing vocabulary {0} in workspace {1} ({2})", iri,
                    workspace.getLabel(), workspace.getUri().toString()));
            } catch (IllegalArgumentException e) {
                throw new PublicationException("Invalid vocabulary IRI " + iri);
            }
        }
    }

    private void clearVocabularyFolder(Git git, VocabularyFolder folder) {
        final File[] files = folder.toPruneAllExceptCompact();
        if (files != null) {
            Arrays.stream(files).forEach(
                file -> githubService.delete(git, file)
            );
        }
    }
}