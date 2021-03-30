package com.github.sgov.server.service.repository;

import com.github.sgov.server.dao.WorkspaceDao;
import com.github.sgov.server.exception.NotFoundException;
import com.github.sgov.server.exception.SGoVException;
import com.github.sgov.server.model.VocabularyContext;
import com.github.sgov.server.model.Workspace;
import java.io.IOException;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.topbraid.shacl.validation.ValidationReport;

/**
 * Service to managed workspaces.
 */
@Service
public class WorkspaceRepositoryService extends BaseRepositoryService<Workspace> {

    WorkspaceDao workspaceDao;

    /**
     * Creates a new repository service.
     */
    @Autowired
    public WorkspaceRepositoryService(
        @Qualifier("validatorFactoryBean") Validator validator,
        WorkspaceDao workspaceDao) {
        super(validator);
        this.workspaceDao = workspaceDao;
    }

    @Override
    protected WorkspaceDao getPrimaryDao() {
        return workspaceDao;
    }

    /**
     * Validates workspace.
     *
     * @param workspace workspace to validate
     * @return report of validation
     */
    public ValidationReport validateWorkspace(Workspace workspace) {
        try {
            return workspaceDao.validateWorkspace(workspace);
        } catch (IOException e) {
            throw new SGoVException(e);
        }
    }


    /**
     * Finds workspace with the specified id and returns it with all its inferred properties.
     *
     * <p>This method guarantees to return a matching
     * instance. If no such object isfound, a {@link NotFoundException} is thrown.
     *
     * @param id Identifier of the workspace to load
     * @return The matching workspace
     * @throws NotFoundException If no matching instance is found
     * @see #find(URI)
     */
    public Workspace findInferred(URI id) {
        Workspace workspace = this.findRequired(id);

        // compute inferences
        workspaceDao.setVocabularyLabels(
            new LinkedList<>(workspace.getVocabularyContexts()),
            "cs"
        );
        return workspace;
    }

    /**
     * Gets the context for the given vocabulary URI in the given workspace, or null if no such
     * context exists.
     *
     * @param workspace  Workspace.
     * @param vocabularyUri URI of the vocabulary for which the context is created.
     * @return true if the vocabulary is already present in the workspace
     */
    @Transactional
    public URI getVocabularyContextReference(
        final Workspace workspace, final URI vocabularyUri) {
        final Optional<URI> vocabularyContextUri = workspace
            .getVocabularyContexts()
            .stream()
            .filter(vc -> vc.getBasedOnVocabularyVersion().equals(vocabularyUri))
            .map(vc -> vc.getUri())
            .findFirst();
        return vocabularyContextUri.orElse(null);
    }

    /**
     * Loads all workspaces including all its inferred properties.
     *
     * @return List of all workspaces
     */
    public List<Workspace> findAllInferred() {
        List<Workspace> workspaces = findAll();
        List<VocabularyContext> vocabularyContexts = workspaces.stream()
            .map(Workspace::getVocabularyContexts)
            .flatMap(Set::stream)
            .collect(Collectors.toList());
        workspaceDao.setVocabularyLabels(vocabularyContexts, "cs");
        return workspaces;
    }

    /**
     * Clears the given vocabulary context.
     *
     * @param vocabularyContext vocabularyContext
     */
    public void clearVocabularyContext(final URI vocabularyContext) {
        workspaceDao.clearVocabularyContext(vocabularyContext);
    }

    public List<URI> getAllDependentVocabularies(final Workspace workspace) {
        return workspaceDao.getAllDependentVocabularies(workspace);
    }
}
