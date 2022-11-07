package com.github.sgov.server.service;

import static com.github.sgov.server.service.WorkspaceUtils.createPullRequestBody;

import com.github.sgov.server.config.conf.FeatureConf;
import com.github.sgov.server.exception.FeatureDisabledException;
import com.github.sgov.server.exception.PublicationException;
import com.github.sgov.server.model.AttachmentContext;
import com.github.sgov.server.model.TrackableContext;
import com.github.sgov.server.model.Workspace;
import com.github.sgov.server.service.repository.GitPublicationService;
import com.github.sgov.server.service.repository.GithubRepositoryService;
import com.github.sgov.server.service.repository.WorkspaceRepositoryService;
import com.github.sgov.server.util.AttachmentFolder;
import com.github.sgov.server.util.Utils;
import com.github.sgov.server.util.Vocabulary;
import com.github.sgov.server.util.VocabularyFolder;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
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

    private final GitPublicationService publicationService;

    private final FeatureConf featureConf;

    /**
     * Constructor.
     */
    @Autowired
    public WorkspacePublicationService(final GithubRepositoryService githubService,
                                       final WorkspaceRepositoryService repositoryService,
                                       final GitPublicationService publicationService,
                                       final FeatureConf featureConf) {
        this.githubService = githubService;
        this.repositoryService = repositoryService;
        this.publicationService = publicationService;
        this.featureConf = featureConf;
    }

    /**
     * Publishes the workspace with the given IRI.
     *
     * @param workspaceUri Workspace that should be published.
     * @return GitHub PR URL
     */
    public URI publish(URI workspaceUri) {
        if (featureConf.isDemo()) {
            throw new FeatureDisabledException("Publishing is disabled by configuration.");
        }
        // flushing to reload workspace instance which might have been modified externally.
        repositoryService.flush();
        final Workspace workspace = repositoryService.findRequired(workspaceUri);
        log.info("Publishing workspace {} with vocabularies {} and attachments {}",
            workspace.getUri(),
            workspace.getVocabularyContexts(),
            workspace.getAllAttachmentContexts());
        final String branchName = WorkspaceUtils.createBranchName(workspace);

        try {
            final File dir = java.nio.file.Files.createTempDirectory("sgov").toFile();
            try (final Git git = githubService.checkout(branchName, dir)) {
                publishVocabularyContexts(git, dir, workspace);
                publishAttachmentContexts(git, dir, workspace);
                githubService.push(git);
                FileUtils.deleteDirectory(dir);
                String prUrl = githubService.createOrUpdatePullRequestToMaster(branchName,
                    MessageFormat.format("Publishing workspace {0} ({1})",
                        workspace.getLabel(),
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
        // changes might come from tools
        repositoryService.flush();
        workspace.getAllAttachmentContexts().forEach(ac -> {
            if (ac.getBasedOnVersion() == null) {
                String uri = ac.getUri().toString();
                String basedOnVersionUri =
                    uri.substring(0, uri.lastIndexOf(Vocabulary.version_separator));
                String uuid =
                    basedOnVersionUri.substring(basedOnVersionUri.lastIndexOf("/") + 1);
                ac.setBasedOnVersion(URI.create(Vocabulary.s_c_priloha + "/" + uuid));
            }
        });
        final Set<URI> attachments = workspace.getAllAttachmentContexts().stream()
            .map(TrackableContext::getBasedOnVersion)
            .collect(Collectors.toSet());
        workspace.getVocabularyContexts().forEach(c -> {
            log.info("Publishing vocabulary context {}", c);
            final URI iri = c.getBasedOnVersion();
            try {
                final VocabularyFolder folder = Utils.getVocabularyFolder(dir, iri.toString());
                deleteFilesFromGit(git, folder.toPruneAllExceptCompact());
                c.setAttachments(attachments);
                publicationService.storeContext(c, folder);
                githubService.commit(git, folder, MessageFormat.format(
                    "Publishing vocabulary {0} in workspace {1} ({2})", iri,
                    workspace.getLabel(), workspace.getUri()));
            } catch (IllegalArgumentException e) {
                throw new PublicationException("Invalid vocabulary IRI " + iri);
            }
        });
    }

    private void publishAttachmentContexts(Git git, File dir, Workspace workspace) {
        for (final AttachmentContext c : workspace.getAllAttachmentContexts()) {
            log.info("Publishing attachment context {}", c);
            final URI iri = c.getBasedOnVersion();
            try {
                final AttachmentFolder folder = Utils.getAttachmentFolder(dir, iri.toString());
                deleteFilesFromGit(git, folder.getAttachmentFile().listFiles());
                publicationService.storeContext(c, folder);
                githubService.commit(git, folder, MessageFormat.format(
                    "Publishing attachment {0} in workspace {1} ({2})", iri,
                    workspace.getLabel(), workspace.getUri()));
            } catch (IllegalArgumentException e) {
                throw new PublicationException("Invalid vocabulary IRI " + iri);
            }
        }
    }

    private void deleteFilesFromGit(final Git git, final File[] files) {
        if (files != null) {
            Arrays.stream(files).forEach(
                file -> githubService.delete(git, file)
            );
        }
    }
}