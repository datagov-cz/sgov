package com.github.sgov.server.service.repository;

import com.github.sgov.server.dao.WorkspaceDao;
import com.github.sgov.server.model.ChangeTrackingContext;
import com.github.sgov.server.model.VocabularyContext;
import com.github.sgov.server.model.Workspace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.topbraid.shacl.validation.ValidationReport;

import javax.validation.Validator;
import java.net.URI;
import java.util.List;

/**
 * Service to managed workspaces.
 */
@Service
public class WorkspaceRepositoryService extends BaseRepositoryService<Workspace> {

    WorkspaceDao workspaceDao;

    @Autowired
    public WorkspaceRepositoryService(Validator validator, WorkspaceDao workspaceDao) {
        super(validator);
        this.workspaceDao = workspaceDao;
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

    @Transactional
    public VocabularyContext createVocabularyContext(URI workspaceUri, URI vocabularyUri, boolean isReadOnly) {
        Workspace workspace = getRequiredReference(workspaceUri);
        VocabularyContext vocabularyContext = new VocabularyContext();
        vocabularyContext.setBasedOnVocabularyVersion(vocabularyUri);
        vocabularyContext.setReadonly(isReadOnly);
        ChangeTrackingContext changeTrackingContext = new ChangeTrackingContext();
        changeTrackingContext.setChangesVocabularyVersion(vocabularyUri);
        vocabularyContext.setChangeTrackingContext(changeTrackingContext);
        workspace.addRefersToVocabularyContexts(vocabularyContext);
        workspaceDao.update(workspace);
        return vocabularyContext;
    }
}
