package com.github.sgov.server.service.repository;

import com.github.sgov.server.config.conf.RepositoryConf;
import com.github.sgov.server.model.VocabularyContext;
import com.github.sgov.server.util.IdnUtils;
import java.net.URI;
import java.net.URISyntaxException;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service to managed workspaces.
 */
@Service
public class VocabularyService {

    RepositoryConf repositoryConf;

    /**
     * Creates a new repository service.
     */
    @Autowired
    public VocabularyService(RepositoryConf repositoryConf) {
        this.repositoryConf = repositoryConf;
    }

    /**
     * Reloads the given vocabulary context from the source endpoint.
     *
     * @param vocabularyContext the vocabulary context to be loaded.
     */
    @Transactional
    public void loadContext(final VocabularyContext vocabularyContext) {
        URI vocabularyVersion = vocabularyContext.getBasedOnVocabularyVersion();
        try {
            SPARQLRepository repo =
                new SPARQLRepository(IdnUtils.convertUnicodeUrlToAscii(
                    repositoryConf.getReleaseSparqlEndpointUrl()));
            ValueFactory f = repo.getValueFactory();
            RepositoryConnection connection = repo.getConnection();
            GraphQuery query = connection
                .prepareGraphQuery("PREFIX : <"
                    + vocabularyVersion
                    + "/> CONSTRUCT {?s ?p ?o} WHERE { GRAPH ?g {?s ?p ?o} FILTER(?g IN (<"
                    + vocabularyVersion
                    + ">,:glosář,:model))}");
            GraphQueryResult result = query.evaluate();
            HTTPRepository workspaceRepository = new HTTPRepository(
                repositoryConf.getUrl());
            RepositoryConnection connection2 = workspaceRepository.getConnection();
            connection2.add((Iterable<Statement>) result,
                f.createIRI(vocabularyContext.getUri().toString()));

            connection.close();
            connection2.close();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
