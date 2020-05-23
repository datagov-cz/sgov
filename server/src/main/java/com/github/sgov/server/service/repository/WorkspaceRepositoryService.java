package com.github.sgov.server.service.repository;

import com.github.sgov.server.dao.WorkspaceDao;
import com.github.sgov.server.model.ChangeTrackingContext;
import com.github.sgov.server.model.VocabularyContext;
import com.github.sgov.server.model.Workspace;
import cz.cvut.kbss.jopa.model.EntityManager;
import java.net.URI;
import java.util.List;
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

    EntityManager em;

    /**
     * Creates a new repository service.
     */
    @Autowired
    public WorkspaceRepositoryService(EntityManager em,
                                      Validator validator,
                                      WorkspaceDao workspaceDao) {
        super(validator);
        this.workspaceDao = workspaceDao;
        this.em = em;
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
     * Creates vocabulary context.
     * @param workspaceUri Id of the workspace.
     * @param vocabularyUri Vocabulary for which context is created.
     * @param isReadOnly True, if vocabulary should be readonly which the workspace.
     * @return
     */
    @Transactional
    public VocabularyContext createVocabularyContext(
            URI workspaceUri, URI vocabularyUri, boolean isReadOnly) {

        ChangeTrackingContext changeTrackingContext = new ChangeTrackingContext();
        changeTrackingContext.setChangesVocabularyVersion(vocabularyUri);
        em.persist(changeTrackingContext);

        VocabularyContext vocabularyContext = new VocabularyContext();
        vocabularyContext.setBasedOnVocabularyVersion(vocabularyUri);
        vocabularyContext.setReadonly(isReadOnly);
        em.persist(vocabularyContext);

        vocabularyContext.setChangeTrackingContext(changeTrackingContext);

        Workspace workspace = getRequiredReference(workspaceUri);
        workspace.addRefersToVocabularyContexts(em.find(VocabularyContext.class,
            vocabularyContext.getUri()));
        workspaceDao.update(workspace);
        return vocabularyContext;
    }
}
