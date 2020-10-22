package com.github.sgov.server.service;

import com.github.sgov.server.exception.NotFoundException;
import com.github.sgov.server.exception.PublicationException;
import com.github.sgov.server.model.ChangeTrackingContext;
import com.github.sgov.server.model.VocabularyContext;
import com.github.sgov.server.model.Workspace;
import com.github.sgov.server.service.repository.GithubRepositoryService;
import com.github.sgov.server.service.repository.VocabularyService;
import com.github.sgov.server.service.repository.WorkspaceRepositoryService;
import com.github.sgov.server.util.VocabularyFolder;
import com.github.sgov.server.util.VocabularyInstance;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.topbraid.shacl.validation.ValidationReport;

/**
 * Workspace-related business logic.
 */
@Service
@Slf4j
public class WorkspaceService {

    private final WorkspaceRepositoryService repositoryService;

    private final VocabularyService vocabularyService;

    private final GithubRepositoryService githubService;

    /**
     * Constructor.
     */
    @Autowired
    public WorkspaceService(WorkspaceRepositoryService repositoryService,
                            VocabularyService vocabularyService,
                            GithubRepositoryService githubService) {
        this.repositoryService = repositoryService;
        this.vocabularyService = vocabularyService;
        this.githubService = githubService;
    }


    public List<String> getAllWorkspaceIris() {
        return repositoryService.getAllWorkspaceIris();
    }

    /**
     * Validates the workspace with the given IRI.
     *
     * @param workspaceUri Workspace that should be created.
     */
    public ValidationReport validate(URI workspaceUri) {
        final Workspace workspace = getWorkspace(workspaceUri);
        return repositoryService.validateWorkspace(workspace);
    }

    private Workspace getWorkspace(URI workspaceUri) {
        final Workspace workspace = repositoryService.findRequired(workspaceUri);
        if (workspace == null) {
            throw new NotFoundException("Vocabulary context " + workspaceUri + " does not exist.");
        }
        return workspace;
    }

    private String createPullRequestBody(final Workspace workspace) {
        return MessageFormat.format("Changed vocabularies: \n - {0}", workspace
            .getVocabularyContexts()
            .stream()
            .map(c -> c.getBasedOnVocabularyVersion().toString() + " (kontext " + c.getUri() + ")")
            .collect(Collectors.joining("\n - "))
        );
    }

    private String createBranchName(final String workspaceUriString) {
        return
            "PL-publish-" + workspaceUriString
                .substring(workspaceUriString.lastIndexOf("/") + 1);
    }

    private void publishContexts(Git git, File dir, Workspace workspace) {
        for (final VocabularyContext c : workspace.getVocabularyContexts()) {
            final URI iri = c.getBasedOnVocabularyVersion();
            try {
                final VocabularyInstance instance = new VocabularyInstance(iri.toString());
                final VocabularyFolder f = VocabularyFolder.ofVocabularyIri(dir, instance);

                // emptying the vocabulary
                final File[] files = f.toPruneAllExceptCompact();
                if (files != null) {
                    Arrays.stream(files).forEach(
                        ff -> {
                            githubService.delete(git, ff);
                        }
                    );
                }

                vocabularyService.storeContext(c, f);
                githubService.commit(git, MessageFormat.format(
                    "Publishing vocabulary {0} in workspace {1} ({2})", iri,
                    workspace.getLabel(), workspace.getUri().toString()));
            } catch (IllegalArgumentException e) {
                throw new PublicationException("Invalid vocabulary IRI " + iri);
            }
        }
    }

    /**
     * Validates the workspace with the given IRI.
     *
     * @param workspaceUri Workspace that should be created.
     * @return GitHub PR URL
     */
    public URI publish(URI workspaceUri) {
        final Workspace workspace = getWorkspace(workspaceUri);

        final String workspaceUriString = workspace.getUri().toString();
        final String branchName = createBranchName(workspaceUriString);

        final File dir = Files.createTempDir();

        try (final Git git = githubService.checkout(branchName, dir)) {
            publishContexts(git, dir, workspace);

            githubService.push(git);

            try {
                FileUtils.deleteDirectory(dir);
            } catch (IOException e) {
                throw new PublicationException("An exception occurred during publishing workspace.",
                    e);
            }

            String prUrl = githubService.createOrUpdatePullRequestToMaster(branchName,
                MessageFormat.format("Publishing workspace {0} ({1})", workspace.getLabel(),
                    workspaceUriString),
                createPullRequestBody(workspace));

            return URI.create(prUrl);
        }
    }

    public Workspace persist(Workspace instance) {
        repositoryService.persist(instance);
        return instance;
    }

    public Workspace findInferred(URI id) {
        return repositoryService.findInferred(id);
    }

    /**
     * Updates only direct attributes of the workspace.
     *
     * @param workspace Workspace that holds updated attributes.
     */
    public void update(Workspace workspace) {
        Workspace update = repositoryService.findRequired(workspace.getUri());
        update.setLabel(workspace.getLabel());
        repositoryService.update(update);
    }

    public void remove(URI id) {
        repositoryService.remove(id);
    }

    public Workspace getRequiredReference(URI id) {
        return repositoryService.getRequiredReference(id);
    }

    private VocabularyContext stub(URI vocabularyUri) {
        final VocabularyContext vocabularyContext = new VocabularyContext();
        vocabularyContext.setBasedOnVocabularyVersion(vocabularyUri);
        ChangeTrackingContext changeTrackingContext = new ChangeTrackingContext();
        changeTrackingContext.setChangesVocabularyVersion(vocabularyUri);
        vocabularyContext.setChangeTrackingContext(changeTrackingContext);
        return vocabularyContext;
    }

    /**
     * Ensures that a vocabulary with the given IRI is registered in the workspace. - If the
     * vocabulary does not exist, an error is thrown. - if the vocabulary exists and is part of the
     * workspace, nothing happens, and the content is left intact. - if the vocabulary exists, is
     * NOT part of the workspace, and should be added as R/W it is only added to the workspace if no
     * other workspace is registering the vocabulary in R/W. - if the vocabulary exists and is NOT
     * part of the workspace, it is added to the workspace and its content is loaded.
     *
     * @param workspaceUri  URI of the workspace to connect the vocabulary context to.
     * @param vocabularyUri URI of the vocabulary to be attached to the workspace
     * @param isReadOnly    true if the context should be created as read-only (no effect if the
     *                      vocabulary already exists)
     * @return URI of the vocabulary context to create
     */
    public URI ensureVocabularyExistsInWorkspace(
        URI workspaceUri, URI vocabularyUri, boolean isReadOnly, String label) {
        URI vocabularyContextUri =
            repositoryService.getVocabularyContextReference(workspaceUri, vocabularyUri);
        if (vocabularyContextUri != null) {
            return vocabularyContextUri;
        }

        VocabularyContext vocabularyContext;
        Workspace workspace = repositoryService.findRequired(workspaceUri);

        if (!vocabularyService.getVocabulariesAsContextDtos().stream()
            .filter(vc ->
                vc.getBasedOnVocabularyVersion().equals(vocabularyUri)
            ).findAny().isPresent()
        ) {
            if (label != null) {
                vocabularyContext = stub(vocabularyUri);
                vocabularyContext.setReadonly(isReadOnly);
                workspace.addRefersToVocabularyContexts(vocabularyContext);
                repositoryService.update(workspace);
                vocabularyContextUri =
                    repositoryService.getVocabularyContextReference(workspaceUri, vocabularyUri);
                vocabularyContext = vocabularyService.findRequired(vocabularyContextUri);
                vocabularyService.createContext(vocabularyContext, label);
            } else {
                throw NotFoundException.create("Vocabulary", vocabularyUri);
            }
        } else {
            vocabularyContext = stub(vocabularyUri);
            vocabularyContext.setReadonly(isReadOnly);
            workspace.addRefersToVocabularyContexts(vocabularyContext);
            repositoryService.update(workspace);
            vocabularyContextUri = vocabularyContext.getUri();
            vocabularyService.loadContext(vocabularyContext);
        }

        return vocabularyContextUri;
    }

    /**
     * Collects workspaces which have the given vocabulary attached in R/W mode.
     *
     * @param vocabularyIri IRI of the vocabulary to be checked
     * @return list of workspaces
     */
    public Collection<Workspace> getWorkspacesWithReadWriteVocabulary(final URI vocabularyIri) {
        return repositoryService.findAll().stream()
            .filter(ws -> ws.getVocabularyContexts()
                .stream()
                .filter(vc -> vc.getBasedOnVocabularyVersion().equals(vocabularyIri))
                .filter(vc -> !vc.isReadonly())
                .findAny()
                .isPresent()
            ).collect(Collectors.toList());
    }

    public List<Workspace> findAllInferred() {
        return repositoryService.findAllInferred();
    }

    /**
     * Removes vocabulary context from given workspace.
     *
     * @param workspaceId         Uri of a workspace.
     * @param vocabularyContextId Uri of a vocabulary context.
     */
    public VocabularyContext removeVocabulary(URI workspaceId, URI vocabularyContextId) {
        Workspace workspace = repositoryService.findRequired(workspaceId);
        VocabularyContext vocabularyContext = workspace.getVocabularyContexts().stream()
            .filter(vc -> vc.getUri().equals(vocabularyContextId))
            .findFirst().orElseThrow(
                () -> NotFoundException.create(
                    VocabularyContext.class.getSimpleName(), vocabularyContextId
                )
            );
        ChangeTrackingContext changeTrackingContext = vocabularyContext.getChangeTrackingContext();
        repositoryService.clearVocabularyContext(changeTrackingContext.getUri());
        repositoryService.clearVocabularyContext(vocabularyContextId);

        vocabularyService.remove(vocabularyContext);

        workspace.getVocabularyContexts().remove(vocabularyContext);
        repositoryService.update(workspace);

        return vocabularyContext;
    }
}
