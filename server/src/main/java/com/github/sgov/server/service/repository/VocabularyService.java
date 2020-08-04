package com.github.sgov.server.service.repository;

import com.github.sgov.server.config.conf.RepositoryConf;
import com.github.sgov.server.exception.SGoVException;
import com.github.sgov.server.model.VocabularyContext;
import com.github.sgov.server.util.IdnUtils;
import com.github.sgov.server.util.Vocabulary;
import com.github.sgov.server.util.VocabularyFolder;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.TupleQuery;
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
     * Finds all vocabularies which are published with optional label in the given language.
     * @param lang language to fetch the label in
     * @return vocabularies in the form of vocabulary context
     */
    public List<VocabularyContext> findAll(String lang) {
        try {
            List<VocabularyContext> contexts = new ArrayList<>();
            final SPARQLRepository repo =
                new SPARQLRepository(IdnUtils.convertUnicodeUrlToAscii(
                    repositoryConf.getReleaseSparqlEndpointUrl()));
            final RepositoryConnection connection = repo.getConnection();
            TupleQuery query = connection
                .prepareTupleQuery("SELECT ?g ?label WHERE "
                    + "{ GRAPH ?g {?g a <" + Vocabulary.s_c_slovnik + "> . "
                    + "OPTIONAL { ?g <http://purl.org/dc/terms/title> ?label . "
                    + "FILTER (lang(?label)='" + lang + "') }}}");
            query.evaluate().forEach(b -> {
                final VocabularyContext c = new VocabularyContext();
                c.setUri(URI.create(b.getValue("g").stringValue()));
                if (b.hasBinding("label")) {
                    c.setLabel(b.getValue("label").stringValue());
                }
                contexts.add(c);
            });
            connection.close();
            return contexts;
        } catch (URISyntaxException e) {
            throw new SGoVException(e);
        }
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
            throw new SGoVException(e);
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
     * Stores a vocabulary into the given vocabulary folder.
     *
     * @param conWorkspace           given workspace repository
     * @param vocabularyVersionUrl   required vocabulary version
     * @param ctxWorkspaceVocabulary context to export
     * @param folder                 vocabulary folder
     * @throws FileNotFoundException whenever the respective files cannot be found in the vocabulary
     *                               folder
     */
    public void storeRepo(RepositoryConnection conWorkspace,
                          String vocabularyVersionUrl,
                          IRI ctxWorkspaceVocabulary,
                          VocabularyFolder folder) throws FileNotFoundException {

        final MemoryStore sspStore = new MemoryStore();
        final Repository sspRepo = new SailRepository(sspStore);
        final RepositoryConnection conGitSsp = sspRepo.getConnection();

        ValueFactory fsspRepo = conGitSsp.getValueFactory();
        IRI ctxVocabulary = fsspRepo.createIRI(vocabularyVersionUrl);
        IRI ctxGlossary = fsspRepo.createIRI(vocabularyVersionUrl + "/glosář");
        IRI ctxModel = fsspRepo.createIRI(vocabularyVersionUrl + "/model");

        conWorkspace.getStatements(ctxVocabulary, null, null, ctxWorkspaceVocabulary)
            .stream()
            .forEach(
                s -> conGitSsp.add(s, ctxVocabulary)
            );

        conWorkspace.getStatements(null, null, null, ctxWorkspaceVocabulary)
            .stream()
            .filter(s -> !s.getSubject().equals(ctxVocabulary))
            .forEach(s -> {
                if ((s.getPredicate().equals(RDF.TYPE)
                    && s.getPredicate().getNamespace().equals(SKOS.NAMESPACE))
                    || s.getPredicate().getNamespace().equals(SKOS.NAMESPACE)
                ) {
                    conGitSsp.add(s, ctxGlossary);
                } else {
                    conGitSsp.add(s, ctxModel);
                }
            });

        File vocFile = folder.getVocabularyFile("");
        conGitSsp.export(
            Rio.createWriter(RDFFormat.TURTLE,
                new FileOutputStream(vocFile)),
            ctxVocabulary);

        File gloFile = folder.getGlossaryFile("");
        conGitSsp.export(
            Rio.createWriter(RDFFormat.TURTLE,
                new FileOutputStream(gloFile)),
            ctxGlossary);

        File modFile = folder.getModelFile("");
        conGitSsp.export(
            Rio.createWriter(RDFFormat.TURTLE,
                new FileOutputStream(modFile)),
            ctxModel);

        conGitSsp.close();
        conWorkspace.close();
    }

    /**
     * Reloads the given vocabulary context from the source endpoint.
     *
     * @param vocabularyContext the vocabulary context to be loaded.
     */
    @Transactional
    public void storeContext(final VocabularyContext vocabularyContext,
                             final VocabularyFolder vocabularyFolder) {
        try {
            final SPARQLRepository workspaceRepo =
                new SPARQLRepository(IdnUtils.convertUnicodeUrlToAscii(
                    repositoryConf.getUrl()));
            final RepositoryConnection cWorkspaceRepo = workspaceRepo.getConnection();


            String vocabularyVersionUrl =
                vocabularyContext.getBasedOnVocabularyVersion().toString();

            IRI ctxWorkspaceVocabulary =
                cWorkspaceRepo.getValueFactory().createIRI(vocabularyContext.getUri().toString());

            storeRepo(cWorkspaceRepo,
                vocabularyVersionUrl,
                ctxWorkspaceVocabulary,
                vocabularyFolder);
        } catch (URISyntaxException e) {
            throw new SGoVException(e);
        } catch (FileNotFoundException e) {
            throw new SGoVException(e);
        }
    }
}
