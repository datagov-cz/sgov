package com.github.sgov.server.service.repository;

import com.github.sgov.server.config.conf.RepositoryConf;
import com.github.sgov.server.dao.VocabularyDao;
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
import javax.validation.Validator;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
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
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.WriterConfig;
import org.eclipse.rdf4j.rio.helpers.BasicParserSettings;
import org.eclipse.rdf4j.rio.helpers.BasicWriterSettings;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service to managed workspaces.
 */
@Service
public class VocabularyService extends BaseRepositoryService<VocabularyContext> {

    RepositoryConf repositoryConf;

    VocabularyDao vocabularyDao;

    /**
     * Creates a new repository service.
     */
    @Autowired
    public VocabularyService(@Qualifier("validatorFactoryBean") Validator validator,
                             RepositoryConf repositoryConf,
                             VocabularyDao vocabularyDao) {
        super(validator);
        this.repositoryConf = repositoryConf;
        this.vocabularyDao = vocabularyDao;
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

    private RDFWriter getWriter(File file) throws FileNotFoundException {
        RDFWriter writer = Rio.createWriter(RDFFormat.TURTLE,
            new FileOutputStream(file));
        writer.setWriterConfig(new WriterConfig()
            .set(BasicWriterSettings.PRETTY_PRINT, true)
            .set(BasicWriterSettings.INLINE_BLANK_NODES, true));
        return writer;
    }

    private void addNamespaces(RepositoryConnection conGitSsp) {
        conGitSsp.setNamespace("rdf", RDF.NAMESPACE);
        conGitSsp.setNamespace("owl", OWL.NAMESPACE);
        conGitSsp.setNamespace("rdfs", RDFS.NAMESPACE);
        conGitSsp.setNamespace("dcterms", DCTERMS.NAMESPACE);
        conGitSsp.setNamespace("xsd", XMLSchema.NAMESPACE);
        conGitSsp.setNamespace("bibo", "http://purl.org/ontology/bibo/");
        conGitSsp.setNamespace("vann", "http://purl.org/vocab/vann/");
        conGitSsp.setNamespace("z-sgov",  "https://slovník.gov.cz/základní/");
        conGitSsp.setNamespace("z-sgov-pojem",  "https://slovník.gov.cz/základní/pojem/");
        conGitSsp.setNamespace("vann", "http://purl.org/vocab/vann/");
        conGitSsp.setNamespace("a-popis-dat-pojem",
            "http://onto.fel.cvut.cz/ontologies/slovník/agendový/popis-dat/pojem/");
        conGitSsp.setNamespace("skos",
            "http://www.w3.org/2004/02/skos/core#");
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

        addNamespaces(conGitSsp);

        ValueFactory fsspRepo = conGitSsp.getValueFactory();
        IRI ctxVocabulary = fsspRepo.createIRI(vocabularyVersionUrl);

        conGitSsp.setNamespace(folder.getVocabularyId() + "-pojem",
            ctxVocabulary.toString() + "/pojem/");
        conGitSsp.setNamespace(folder.getVocabularyId(), ctxVocabulary.toString() + "/");

        conWorkspace.getStatements(ctxVocabulary, null, null, ctxWorkspaceVocabulary)
            .stream()
            .forEach(
                s -> conGitSsp.add(s, ctxVocabulary)
            );

        IRI ctxGlossary = fsspRepo.createIRI(vocabularyVersionUrl + "/glosář");
        conWorkspace.getStatements(ctxGlossary, null, null, ctxWorkspaceVocabulary)
            .stream()
            .forEach(
                s -> conGitSsp.add(s, ctxGlossary)
            );

        IRI ctxModel = fsspRepo.createIRI(vocabularyVersionUrl + "/model");
        conWorkspace.getStatements(ctxModel, null, null, ctxWorkspaceVocabulary)
            .stream()
            .forEach(
                s -> conGitSsp.add(s, ctxModel)
            );

        conWorkspace.getStatements(null, null, null, ctxWorkspaceVocabulary)
            .stream()
            .filter(s -> !s.getSubject().equals(ctxVocabulary))
            .filter(s -> !s.getSubject().equals(ctxGlossary))
            .filter(s -> !s.getSubject().equals(ctxModel))
            .filter(s -> !s.getSubject().stringValue().startsWith(Vocabulary.ONTOGRAPHER_NAMESPACE))
            .forEach(s -> {
                if (((s.getObject() instanceof IRI)
                    && ((IRI) s.getObject()).getNamespace().equals(SKOS.NAMESPACE))
                    || s.getPredicate().getNamespace().equals(SKOS.NAMESPACE)
                ) {
                    conGitSsp.add(s, ctxGlossary);
                } else {
                    conGitSsp.add(s, ctxModel);
                }
            });

        File vocFile = folder.getVocabularyFile("");
        conGitSsp.export(getWriter(vocFile), ctxVocabulary);

        File gloFile = folder.getGlossaryFile("");
        conGitSsp.export(getWriter(gloFile), ctxGlossary);

        File modFile = folder.getModelFile("");
        conGitSsp.export(getWriter(modFile), ctxModel);

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

    @Override
    protected VocabularyDao getPrimaryDao() {
        return vocabularyDao;
    }
}
