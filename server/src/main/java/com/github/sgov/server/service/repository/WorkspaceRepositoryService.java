package com.github.sgov.server.service.repository;

import com.github.sgov.server.config.conf.RepositoryConf;
import com.github.sgov.server.dao.WorkspaceDao;
import com.github.sgov.server.exception.NotFoundException;
import com.github.sgov.server.model.ChangeTrackingContext;
import com.github.sgov.server.model.VocabularyContext;
import com.github.sgov.server.model.Workspace;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.topbraid.shacl.validation.ValidationReport;

/**
 * Service to managed workspaces.
 */
@Service
public class WorkspaceRepositoryService extends BaseRepositoryService<Workspace> {

    WorkspaceDao workspaceDao;

    RepositoryConf repositoryConf;

    /**
     * Creates a new repository service.
     */
    @Autowired
    public WorkspaceRepositoryService(Validator validator,
                                      WorkspaceDao workspaceDao,
                                      RepositoryConf repositoryConf) {
        super(validator);
        this.workspaceDao = workspaceDao;
        this.repositoryConf = repositoryConf;
    }

    public List<String> getAllWorkspaceIris() {
        return workspaceDao.getAllWorkspaceIris();
    }

    @Override
    protected WorkspaceDao getPrimaryDao() {
        return workspaceDao;
    }

    public ValidationReport validateWorkspace(String workspaceIri) {
        return workspaceDao.validateWorkspace(workspaceIri);
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
     * Creates vocabulary context.
     *
     * @param workspaceUri  Id of the workspace.
     * @param vocabularyUri Vocabulary for which context is created.
     * @param isReadOnly    True, if vocabulary should be readonly which the workspace.
     * @return
     */
    @Transactional
    public VocabularyContext createVocabularyContext(
        URI workspaceUri, URI vocabularyUri, boolean isReadOnly) {

        VocabularyContext vocabularyContext = new VocabularyContext();
        vocabularyContext.setBasedOnVocabularyVersion(vocabularyUri);
        vocabularyContext.setReadonly(isReadOnly);
        ChangeTrackingContext changeTrackingContext = new ChangeTrackingContext();
        changeTrackingContext.setChangesVocabularyVersion(vocabularyUri);
        vocabularyContext.setChangeTrackingContext(changeTrackingContext);

        Workspace workspace = getRequiredReference(workspaceUri);
        workspace.addRefersToVocabularyContexts(vocabularyContext);
        workspaceDao.update(workspace);
        return vocabularyContext;
    }

    /**
     * Gets the context for the given vocabulary URI in the given workspace, or null if no such
     * context exists.
     *
     * @param workspaceUri  URI of the workspace.
     * @param vocabularyUri URI of the vocabulary for which the context is created.
     * @return true if the vocabulary is already present in the workspace
     */
    @Transactional
    public URI getVocabularyContextReference(
        final URI workspaceUri, final URI vocabularyUri) {
        final Workspace workspace = findRequired(workspaceUri);
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
}
