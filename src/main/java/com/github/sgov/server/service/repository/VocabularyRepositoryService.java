package com.github.sgov.server.service.repository;

import static com.github.sgov.server.util.Constants.SERIALIZATION_LANGUAGE;

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
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.eclipse.rdf4j.rio.ParserConfig;
import org.eclipse.rdf4j.rio.helpers.BasicParserSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service to managed workspaces.
 */
@Service
public class VocabularyRepositoryService extends BaseRepositoryService<VocabularyContext> {

    private final RepositoryConf repositoryConf;

    private final VocabularyDao vocabularyDao;

    private final WorkspaceDao workspaceDao;

    /**
     * Creates a new repository service.
     */
    @Autowired
    public VocabularyRepositoryService(@Qualifier("validatorFactoryBean") Validator validator,
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
        workspaceDao.findAll().forEach(w -> w.getAssetContexts()
            .forEach(vc -> result.add(vc.getBasedOnVersion())));
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
            .getBasedOnVersion().toString());

        VocabularyCreationHelper.createVocabulary(
            f,
            vocabulary.toString(),
            vocabularyContextDto,
            statements,
            SERIALIZATION_LANGUAGE
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
        final URI vocabularyVersion = vocabularyContext.getBasedOnVersion();
        final GraphQuery query = connection
            .prepareGraphQuery("PREFIX : <"
                + vocabularyVersion
                + "/> CONSTRUCT {?s ?p ?o} WHERE { GRAPH ?g {?s ?p ?o} FILTER(?g IN (<"
                + vocabularyVersion
                + ">,:glosář,:model))}");
        return query.evaluate();
    }

    @Override
    protected VocabularyDao getPrimaryDao() {
        return vocabularyDao;
    }
}
