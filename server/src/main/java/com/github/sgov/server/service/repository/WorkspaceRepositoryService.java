package com.github.sgov.server.service.repository;

import com.github.sgov.server.config.conf.RepositoryConf;
import com.github.sgov.server.dao.WorkspaceDao;
import com.github.sgov.server.model.ChangeTrackingContext;
import com.github.sgov.server.model.VocabularyContext;
import com.github.sgov.server.model.Workspace;
import com.github.sgov.server.util.IdnUtils;
import cz.cvut.kbss.jopa.model.EntityManager;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import javax.validation.Validator;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
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

    RepositoryConf repositoryConf;

    /**
     * Creates a new repository service.
     */
    @Autowired
    public WorkspaceRepositoryService(EntityManager em,
                                      Validator validator,
                                      WorkspaceDao workspaceDao,
                                      RepositoryConf repositoryConf) {
        super(validator);
        this.workspaceDao = workspaceDao;
        this.em = em;
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

    /**
     * Gets the context for the given vocabulary URI in the given workspace, or null if no such
     * context exists.
     *
     * @param workspaceUri URI of the workspace.
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
     * Reloads the given vocabulary context from the source endpoint.
     *
     * @param vocabularyContextUri the vocabulary context to be loaded.
     * @return version of the loaded vocabulary
     */
    @Transactional
    public URI loadContext(final URI vocabularyContextUri, final URI namedGraphUri) {
        try {
            SPARQLRepository repo =
                new SPARQLRepository(IdnUtils.convertUnicodeUrlToAscii(
                    repositoryConf.getReleaseSparqlEndpointUrl()));
            ValueFactory f = repo.getValueFactory();
            RepositoryConnection connection  = repo.getConnection();
            GraphQuery query = connection
                .prepareGraphQuery("CONSTRUCT {?s ?p ?o} WHERE { GRAPH ?g {?s ?p ?o} }");
            query.setBinding("g", f.createIRI(namedGraphUri.toString()));
            GraphQueryResult result = query.evaluate();

            HTTPRepository workspaceRepository = new HTTPRepository(
                repositoryConf.getUrl());
            RepositoryConnection connection2 = workspaceRepository.getConnection();
            connection2.add((Iterable<Statement>) result,
                f.createIRI(vocabularyContextUri.toString()));

            URI realVocabularyVersion = getVocabularyVersion(connection, namedGraphUri);

            connection.close();
            connection2.close();

            return realVocabularyVersion;
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    private URI getVocabularyVersion(RepositoryConnection connection, final URI namedGraphUri) {
        TupleQuery query = connection
            .prepareTupleQuery("SELECT ?versionIri WHERE { GRAPH ?graph {?s ?p ?o} }");
        query.setBinding("graph", connection.getValueFactory().createIRI(namedGraphUri.toString()));
        TupleQueryResult result = query.evaluate();
        return URI.create(result.next().getValue("versionIri").stringValue());
    }
}
