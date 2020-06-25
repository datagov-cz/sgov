package com.github.sgov.server.service;

import com.github.sgov.server.exception.NotFoundException;
import com.github.sgov.server.exception.PublicationException;
import com.github.sgov.server.model.VocabularyContext;
import com.github.sgov.server.model.Workspace;
import com.github.sgov.server.service.repository.VocabularyService;
import com.github.sgov.server.service.repository.WorkspaceRepositoryService;
import com.github.sgov.server.service.security.SecurityUtils;
import com.github.sgov.server.util.VocabularyFolder;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.topbraid.shacl.validation.ValidationReport;

/**
 * Workspace-related business logic.
 */
@Service
public class WorkspaceService {

    private final WorkspaceRepositoryService repositoryService;

    private final VocabularyService vocabularyService;

    private final SecurityUtils securityUtils;

    /**
     * Constructor.
     */
    @Autowired
    public WorkspaceService(WorkspaceRepositoryService repositoryService,
                            VocabularyService vocabularyService,
                            SecurityUtils securityUtils) {
        this.repositoryService = repositoryService;
        this.vocabularyService = vocabularyService;
        this.securityUtils = securityUtils;
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
        final Workspace workspace = repositoryService.findRequired(workspaceUri);
        if (workspace == null) {
            throw new NotFoundException("Vocabulary context " + workspaceUri + " does not exist.");
        }

        return repositoryService.validateWorkspace(workspace);
    }

    /**
     * Validates the workspace with the given IRI.
     *
     * @param workspaceUri Workspace that should be created.
     * @return GitHub PR URL
     */
    public URL publish(URI workspaceUri) {
        final Workspace workspace = repositoryService.findRequired(workspaceUri);
        if (workspace == null) {
            throw new NotFoundException("Vocabulary context " + workspaceUri + " does not exist.");
        }

        final String workspaceUriString = workspaceUri.toString();

        try {
            File dir = Files.createTempDir();
            Git git = Git.cloneRepository()
                .setURI("https://github.com/opendata-mvcr/ssp")
                .setDirectory(dir)
                .call();

            final String branchName =
                "PL-publish-" + workspaceUriString
                    .substring(workspaceUriString.lastIndexOf("/") + 1);
            git.branchCreate()
                .setName(branchName)
                .call();

            git.checkout().setName(branchName).call();

            for (final VocabularyContext c : workspace.getVocabularyContexts()) {
                final URI iri = c.getBasedOnVocabularyVersion();
                final VocabularyFolder f = VocabularyFolder.ofVocabularyIri(dir, iri);

                vocabularyService.storeContext(c, f);

                git.commit()
                    .setAll(true)
                    .setAuthor(securityUtils.getCurrentUser().getFirstName()
                            + " " + securityUtils
                            .getCurrentUser().getLastName(),
                        securityUtils.getCurrentUser().getUsername())
                    .setMessage("Publishing workspace " + workspaceUriString)
                    .call();
            }

            git.push()
                .call();

            return new URL("https://github.com/opendata-mvcr/ssp/pull/1");
        } catch (IOException | GitAPIException e) {
            throw new PublicationException("An exception occurred during publishing workspace.", e);
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

    /**
     * Ensures that a vocabulary with the given IRI is registered in the workspace. If yes, its
     * content is kept intact. Otherwise a new context is created and the content is loaded from
     *
     * @param workspaceUri  URI of the workspace to connect the vocabulary context to.
     * @param vocabularyUri URI of the vocabulary to be attached to the workspace
     * @param isReadOnly    true if the context should be created as read-only (no effect if the
     *                      vocabulary already exists)
     * @return URI of the vocabulary context to create
     */
    public URI ensureVocabularyExistsInWorkspace(
        URI workspaceUri, URI vocabularyUri, boolean isReadOnly) {
        URI vocabularyContextUri =
            repositoryService.getVocabularyContextReference(workspaceUri, vocabularyUri);
        if (vocabularyContextUri == null) {
            VocabularyContext vocabularyContext =
                repositoryService.createVocabularyContext(workspaceUri, vocabularyUri, isReadOnly);
            vocabularyService.loadContext(vocabularyContext);
            vocabularyContextUri = vocabularyContext.getUri();
        }

        return vocabularyContextUri;
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
        workspace.getVocabularyContexts().remove(vocabularyContext);
        repositoryService.update(workspace);
        return vocabularyContext;
    }
}
