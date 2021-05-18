package com.github.sgov.server.service.repository;

import com.github.sgov.server.config.conf.RepositoryConf;
import com.github.sgov.server.controller.dto.VocabularyContextDto;
import com.github.sgov.server.controller.dto.VocabularyDto;
import com.github.sgov.server.dao.VocabularyDao;
import com.github.sgov.server.dao.WorkspaceDao;
import com.github.sgov.server.exception.SGoVException;
import com.github.sgov.server.model.VocabularyContext;
import com.github.sgov.server.util.IdnUtils;
import com.github.sgov.server.util.Vocabulary;
import com.github.sgov.server.util.VocabularyCreationHelper;
import com.github.sgov.server.util.VocabularyFolder;
import com.github.sgov.server.util.VocabularyInstance;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.validation.Validator;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.eclipse.rdf4j.rio.ParserConfig;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.WriterConfig;
import org.eclipse.rdf4j.rio.helpers.BasicParserSettings;
import org.eclipse.rdf4j.rio.helpers.BasicWriterSettings;
import org.eclipse.rdf4j.rio.turtle.ArrangedWriter;
import org.eclipse.rdf4j.rio.turtle.TurtleWriter;
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

    private final RepositoryConf repositoryConf;

    private final VocabularyDao vocabularyDao;

    private final WorkspaceDao workspaceDao;

    /**
     * Creates a new repository service.
     */
    @Autowired
    public VocabularyService(@Qualifier("validatorFactoryBean") Validator validator,
                             RepositoryConf repositoryConf,
                             VocabularyDao vocabularyDao,
                             WorkspaceDao workspaceDao) {
        super(validator);
        this.repositoryConf = repositoryConf;
        this.vocabularyDao = vocabularyDao;
        this.workspaceDao = workspaceDao;
    }

    /**
     * Returns a set of URIs of vocabularies imported by the vocabulary URI of which is provided.
     *
     * @param uri URI of the vocabulary to get transitive imports for.
     * @return a set of transitive imports.
     */
    public Set<URI> getTransitiveImports(final URI uri) {
        try {
            Set<URI> contexts = new HashSet<>();
            final SPARQLRepository repo =
                new SPARQLRepository(IdnUtils.convertUnicodeUrlToAscii(
                    repositoryConf.getReleaseSparqlEndpointUrl()));
            final RepositoryConnection connection = repo.getConnection();
            final TupleQuery query = connection
                .prepareTupleQuery("SELECT DISTINCT ?v WHERE {?uri ?imports+ ?v}");
            query.setBinding("uri", connection.getValueFactory().createIRI(uri.toString()));
            query.setBinding("imports", connection.getValueFactory()
                .createIRI(Vocabulary.DATA_DESCRIPTION_NAMESPACE + "importuje-slovník"));

            query.evaluate().forEach(b ->
                contexts.add(URI.create(b.getValue("v").stringValue())));
            connection.close();
            return contexts;
        } catch (URISyntaxException e) {
            throw new SGoVException(e);
        }
    }


    public List<VocabularyDto> getVocabulariesAsContextDtos() {
        return getVocabulariesAsContextDtos(null);
    }

    /**
     * Finds all vocabularies which are published with optional label in the given language.
     *
     * @param lang language to fetch the label in
     * @return vocabularies in the form of vocabulary context
     */
    public List<VocabularyDto> getVocabulariesAsContextDtos(String lang) {
        try {
            List<VocabularyDto> contexts = new ArrayList<>();
            final SPARQLRepository repo =
                new SPARQLRepository(IdnUtils.convertUnicodeUrlToAscii(
                    repositoryConf.getReleaseSparqlEndpointUrl()));
            final RepositoryConnection connection = repo.getConnection();
            TupleQuery query = connection
                .prepareTupleQuery("SELECT DISTINCT ?g ?label WHERE "
                    + "{ GRAPH ?g {?g a <" + Vocabulary.s_c_slovnik + "> . "
                    + " ?g <" + DCTERMS.TITLE + "> ?label . "
                    + ((lang != null) ? "FILTER (lang(?label)='" + lang + "')" : "")
                    + " }} ORDER BY ?label");
            final Set<URI> uris = getWriteLockedVocabularies();
            query.evaluate().forEach(b -> {
                final VocabularyDto c = new VocabularyDto();
                final URI uri = URI.create(b.getValue("g").stringValue());
                c.setBasedOnVocabularyVersion(uri);
                c.setReadonly(uris.contains(uri));
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
     * Returns the list of vocabularies which are write-locked (i.e. they are writable in some
     * workspace).
     */
    private Set<URI> getWriteLockedVocabularies() {
        final Set<URI> result = new HashSet<>();
        workspaceDao.findAll().forEach(w -> w.getVocabularyContexts().stream()
            .forEach(vc -> result.add(vc.getBasedOnVocabularyVersion())));
        return result;
    }

    /**
     * Reloads the given vocabulary context from the source endpoint.
     *
     * @param vocabularyContext the vocabulary context to be loaded.
     */
    private void populateContext(final VocabularyContext vocabularyContext,
                                 final Iterable<? extends Statement> statements) {
        final HTTPRepository workspaceRepository = new HTTPRepository(
            repositoryConf.getUrl());
        final RepositoryConnection connection2 = workspaceRepository.getConnection();
        connection2.setParserConfig(
            new ParserConfig().set(BasicParserSettings.PRESERVE_BNODE_IDS, true));

        connection2.begin();
        final ValueFactory f = connection2.getValueFactory();
        connection2.add(statements,
            f.createIRI(vocabularyContext.getUri().toString()));
        connection2.commit();
        connection2.close();
    }

    /**
     * Reloads the given vocabulary context from the source endpoint.
     *
     * @param vocabularyContext the vocabulary context to be loaded.
     */
    @Transactional
    public void createContext(final VocabularyContext vocabularyContext,
                              final VocabularyContextDto vocabularyContextDto) {
        final Set<Statement> statements = new HashSet<>();
        final HTTPRepository workspaceRepository = new HTTPRepository(
            repositoryConf.getUrl());
        final RepositoryConnection connection2 = workspaceRepository.getConnection();

        final ValueFactory f = connection2.getValueFactory();
        final IRI vocabulary = f.createIRI(vocabularyContext
            .getBasedOnVocabularyVersion().toString());

        final VocabularyInstance i = new VocabularyInstance(vocabulary.toString());

        VocabularyCreationHelper.createVocabulary(
            f,
            i,
            vocabularyContextDto,
            statements
        );

        populateContext(vocabularyContext, statements);
        connection2.close();
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
            populateContext(vocabularyContext, result);
            connection.close();
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
     * Returns a writer which outputs the same Model in the same form all the time.
     *
     * @param w writer to wrap
     * @return deterministic RDFWriter
     */
    public RDFWriter getDeterministicWriter(final Writer w) {
        RDFWriter writer = new ArrangedWriter(
            new TurtleWriter(w), 100);
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
        conGitSsp.setNamespace("xsd", XSD.NAMESPACE);
        conGitSsp.setNamespace("bibo", "http://purl.org/ontology/bibo/");
        conGitSsp.setNamespace("vann", "http://purl.org/vocab/vann/");
        conGitSsp.setNamespace("z-sgov", "https://slovník.gov.cz/základní/");
        conGitSsp.setNamespace("z-sgov-pojem",
            "https://slovník.gov.cz/základní/pojem/");
        conGitSsp.setNamespace("vann", "http://purl.org/vocab/vann/");
        conGitSsp.setNamespace("a-popis-dat-pojem",
            "http://onto.fel.cvut.cz/ontologies/slovník/agendový/popis-dat/pojem/");
        conGitSsp.setNamespace("skos",
            "http://www.w3.org/2004/02/skos/core#");
    }

    /**
     * Decides whether a statement belongs to a glossary or to a model. All statements with
     * predicate or object in SKOS namespace are considered glossary triples.
     *
     * @param statement statement statement to check
     * @return true if the statement should be put to glossary, false otherwise
     */
    private boolean isGlossaryTriple(Statement statement) {
        return ((statement.getObject() instanceof IRI)
            && ((IRI) statement.getObject()).getNamespace().equals(SKOS.NAMESPACE))
            || statement.getPredicate().getNamespace().equals(SKOS.NAMESPACE);
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
    private void storeRepo(RepositoryConnection conWorkspace,
                           String vocabularyVersionUrl,
                           IRI ctxWorkspaceVocabulary,
                           VocabularyFolder folder
    ) throws IOException {

        final MemoryStore sspStore = new MemoryStore();
        final Repository sspRepo = new SailRepository(sspStore);
        final RepositoryConnection conGitSsp = sspRepo.getConnection();

        addNamespaces(conGitSsp);

        final ValueFactory fsspRepo = conGitSsp.getValueFactory();
        final IRI ctxVocabulary = fsspRepo.createIRI(vocabularyVersionUrl);

        conGitSsp.setNamespace(folder.getVocabularyId() + "-pojem",
            ctxVocabulary.toString() + "/pojem/");
        conGitSsp.setNamespace(folder.getVocabularyId(), ctxVocabulary + "/");

        conWorkspace.getStatements(ctxVocabulary, null, null, ctxWorkspaceVocabulary)
            .forEach(s -> conGitSsp.add(s, ctxVocabulary));

        final IRI ctxGlossary = fsspRepo.createIRI(vocabularyVersionUrl + "/glosář");
        conWorkspace.getStatements(ctxGlossary, null, null, ctxWorkspaceVocabulary)
            .forEach(s -> conGitSsp.add(s, ctxGlossary));

        final IRI ctxModel = fsspRepo.createIRI(vocabularyVersionUrl + "/model");
        conWorkspace.getStatements(ctxModel, null, null, ctxWorkspaceVocabulary)
            .forEach(s -> conGitSsp.add(s, ctxModel));

        conWorkspace.getStatements(null, null, null, ctxWorkspaceVocabulary)
            .stream()
            // triples already processed
            .filter(s -> !s.getSubject().equals(ctxVocabulary))
            .filter(s -> !s.getSubject().equals(ctxGlossary))
            .filter(s -> !s.getSubject().equals(ctxModel))
            // triples belonging to tools
            .filter(s -> !s.getPredicate().stringValue()
                .startsWith(Vocabulary.ONTOGRAPHER_NAMESPACE))
            .filter(s -> !s.getObject().stringValue()
                .startsWith(Vocabulary.ONTOGRAPHER_NAMESPACE))
            .filter(s -> !s.getPredicate().stringValue()
                .startsWith(Vocabulary.TERMIT_NAMESPACE))
            .filter(s -> !s.getObject().stringValue()
                .startsWith(Vocabulary.TERMIT_NAMESPACE))
            // all the rest belongs either to glossary or to model
            .forEach(s ->
                conGitSsp.add(s, isGlossaryTriple(s) ? ctxGlossary : ctxModel)
            );

        if (!folder.getFolder().exists()) {
            folder.getFolder().mkdirs();
        }

        final File vocFile = folder.getVocabularyFile("");
        conGitSsp.export(getDeterministicWriter(new FileWriter(vocFile)), ctxVocabulary);

        final File gloFile = folder.getGlossaryFile("");
        conGitSsp.export(getDeterministicWriter(new FileWriter(gloFile)), ctxGlossary);

        final File modFile = folder.getModelFile("");
        conGitSsp.export(getDeterministicWriter(new FileWriter(modFile)), ctxModel);

        conGitSsp.close();
        conWorkspace.close();
    }

    /**
     * Stores the given vocabulary context into the given vocabulary folder.
     *
     * @param vocabularyContext the vocabulary context to be loaded.
     * @param vocabularyFolder  folder to store the context into.
     */
    @Transactional
    public void storeContext(final VocabularyContext vocabularyContext,
                             final VocabularyFolder vocabularyFolder) {
        try {
            final SPARQLRepository workspaceRepo =
                new SPARQLRepository(IdnUtils.convertUnicodeUrlToAscii(
                    repositoryConf.getUrl()));
            final RepositoryConnection cWorkspaceRepo = workspaceRepo.getConnection();

            final String vocabularyVersionUrl =
                vocabularyContext.getBasedOnVocabularyVersion().toString();

            final IRI ctxWorkspaceVocabulary =
                cWorkspaceRepo.getValueFactory().createIRI(vocabularyContext.getUri().toString());

            storeRepo(cWorkspaceRepo,
                vocabularyVersionUrl,
                ctxWorkspaceVocabulary,
                vocabularyFolder);
        } catch (URISyntaxException | IOException e) {
            throw new SGoVException(e);
        }
    }

    @Override
    protected VocabularyDao getPrimaryDao() {
        return vocabularyDao;
    }
}
