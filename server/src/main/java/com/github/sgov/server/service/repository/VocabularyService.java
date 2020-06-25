package com.github.sgov.server.service.repository;

import com.github.sgov.server.config.conf.RepositoryConf;
import com.github.sgov.server.model.VocabularyContext;
import com.github.sgov.server.util.IdnUtils;
import com.github.sgov.server.util.VocabularyFolder;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.eclipse.rdf4j.rio.ParserConfig;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.BasicParserSettings;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

//import org.apache.jena.rdf.model.Model;
//import org.apache.jena.rdf.model.ModelFactory;
//import org.apache.jena.rdf.model.ResourceFactory;

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
        try {
            final SPARQLRepository repo =
                new SPARQLRepository(IdnUtils.convertUnicodeUrlToAscii(
                    repositoryConf.getReleaseSparqlEndpointUrl()));
            final RepositoryConnection connection = repo.getConnection();
            final GraphQueryResult result = loadContext(vocabularyContext, connection);
            final HTTPRepository workspaceRepository = new HTTPRepository(
                repositoryConf.getUrl());
            final RepositoryConnection connection2 = workspaceRepository.getConnection();
            connection2.setParserConfig(
                new ParserConfig().set(BasicParserSettings.PRESERVE_BNODE_IDS, true));

            connection2.begin();

            final ValueFactory f = connection2.getValueFactory();
            connection2.add((Iterable<Statement>) result,
                f.createIRI(vocabularyContext.getUri().toString()));

            connection2.commit();

            connection.close();
            connection2.close();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    GraphQueryResult loadContext(
        final VocabularyContext vocabularyContext,
        final RepositoryConnection connection) {
        URI vocabularyVersion = vocabularyContext.getBasedOnVocabularyVersion();
        GraphQuery query = connection
            .prepareGraphQuery("PREFIX : <"
                + vocabularyVersion
                + "/> CONSTRUCT {?s ?p ?o} WHERE { GRAPH ?g {?s ?p ?o} FILTER(?g IN (<"
                + vocabularyVersion
                + ">,:glosář,:model))}");
        return query.evaluate();
    }

    /**
     * Reloads the given vocabulary context from the source endpoint.
     *
     * @param vocabularyContext the vocabulary context to be loaded.
     */
    @Transactional
    public void storeContext(final VocabularyContext vocabularyContext, final VocabularyFolder f) {
        try {
            final SPARQLRepository workspaceRepo =
                new SPARQLRepository(IdnUtils.convertUnicodeUrlToAscii(
                    repositoryConf.getUrl()));
            final RepositoryConnection cWorkspaceRepo = workspaceRepo.getConnection();
            MemoryStore sspStore = new MemoryStore();
            Repository sspRepo = new SailRepository(sspStore);
            RepositoryConnection sspConnection = sspRepo.getConnection();

            String url = vocabularyContext.getBasedOnVocabularyVersion().toString();
            ValueFactory fsspRepo = sspRepo.getValueFactory();
            IRI ctxVocabulary = fsspRepo.createIRI(url);
            IRI ctxGlossary = fsspRepo.createIRI(url + "/glosář");
            IRI ctxModel = fsspRepo.createIRI(url + "/model");
            IRI ctxWorkspaceVocabulary =
                cWorkspaceRepo.getValueFactory().createIRI(vocabularyContext.getUri().toString());

            cWorkspaceRepo.getStatements(ctxVocabulary, null, null, ctxWorkspaceVocabulary)
                .forEach(
                    s -> sspConnection.add(s, ctxVocabulary)
                );

            cWorkspaceRepo.getStatements(null, null, null, ctxWorkspaceVocabulary)
                .stream()
                .filter(s -> !s.getSubject().equals(ctxVocabulary))
                .forEach(s -> {
                    if ((s.getPredicate().equals(RDF.TYPE)
                        && s.getPredicate().getNamespace().equals(SKOS.NAMESPACE))
                        || s.getPredicate().getNamespace().equals(SKOS.NAMESPACE)
                    ) {
                        sspConnection.add(s, ctxGlossary);
                    } else {
                        sspConnection.add(s, ctxModel);
                    }
                });

            File vocFile = f.getVocabularyFile("");
            sspConnection.export(
                Rio.createWriter(RDFFormat.TURTLE,
                    new FileOutputStream(vocFile)),
                ctxVocabulary);

            File gloFile = f.getGlossaryFile("");
            sspConnection.export(
                Rio.createWriter(RDFFormat.TURTLE,
                    new FileOutputStream(gloFile)),
                ctxGlossary);

            File modFile = f.getModelFile("");
            sspConnection.export(
                Rio.createWriter(RDFFormat.TURTLE,
                    new FileOutputStream(modFile)),
                ctxModel);

            sspConnection.close();
            cWorkspaceRepo.close();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
