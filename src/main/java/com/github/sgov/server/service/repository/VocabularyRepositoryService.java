package com.github.sgov.server.service.repository;

import static com.github.sgov.server.util.Constants.SERIALIZATION_LANGUAGE;

import com.github.sgov.server.config.conf.RepositoryConf;
import com.github.sgov.server.controller.dto.VocabularyContextDto;
import com.github.sgov.server.controller.dto.VocabularyDto;
import com.github.sgov.server.controller.dto.VocabularyStatusDto;
import com.github.sgov.server.controller.dto.VocabularyWithWorkspacesDto;
import com.github.sgov.server.controller.dto.WorkspaceDto;
import com.github.sgov.server.dao.VocabularyDao;
import com.github.sgov.server.dao.WorkspaceDao;
import com.github.sgov.server.exception.SGoVException;
import com.github.sgov.server.model.TrackableContext;
import com.github.sgov.server.model.VocabularyContext;
import com.github.sgov.server.util.IdnUtils;
import com.github.sgov.server.util.Vocabulary;
import com.github.sgov.server.util.VocabularyCreationHelper;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
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
 * Service to managed vocabularies.
 */
@Service
public class VocabularyRepositoryService extends BaseRepositoryService<VocabularyContext> {

    private final RepositoryConf repositoryConf;

    private final VocabularyDao vocabularyDao;

    private final WorkspaceDao workspaceDao;

    private final AttachmentRepositoryService attachmentRepositoryService;

    /**
     * Creates a new repository service.
     */
    @Autowired
    public VocabularyRepositoryService(@Qualifier("validatorFactoryBean") Validator validator,
                                       RepositoryConf repositoryConf,
                                       VocabularyDao vocabularyDao,
                                       WorkspaceDao workspaceDao,
                                       AttachmentRepositoryService attachmentRepositoryService) {
        super(validator);
        this.repositoryConf = repositoryConf;
        this.vocabularyDao = vocabularyDao;
        this.workspaceDao = workspaceDao;
        this.attachmentRepositoryService = attachmentRepositoryService;
    }

    /**
     * Returns a set of vocabulary URIs imported by the provided vocabulary URI.
     *
     * @param uri URI of the vocabulary to get transitive imports for.
     * @return a set of transitive imports.
     */
    public Set<URI> getTransitiveImports(final URI uri) {
        try {
            Set<URI> contexts = new HashSet<>();
            final SPARQLRepository repo =
                new SPARQLRepository(IdnUtils.convertUnicodeUrlToAscii(
                    repositoryConf.getUrl()));
            final RepositoryConnection connection = repo.getConnection();
            final TupleQuery query = connection
                .prepareTupleQuery("SELECT DISTINCT ?v WHERE {GRAPH ?uri {?uri ?imports+ ?v}}");
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
                    repositoryConf.getUrl()));
            final RepositoryConnection connection = repo.getConnection();
            TupleQuery query = connection
                .prepareTupleQuery("SELECT DISTINCT ?g ?label WHERE "
                    + "{ GRAPH ?g {?g a <" + Vocabulary.s_c_slovnik + "> . "
                    + " ?g <" + DCTERMS.TITLE + "> ?label . "
                    + ((lang != null) ? "FILTER (lang(?label)='" + lang + "')" : "")
                    + " }} ORDER BY ?label");
            final Set<URI> uris = getWriteLockedVocabularies();
            query.evaluate().forEach(res -> {
                final VocabularyDto vDto = new VocabularyDto();
                final URI uri = URI.create(res.getValue("g").stringValue());
                vDto.setUri(uri);
                vDto.setBasedOnVersion(uri);
                vDto.setReadonly(uris.contains(uri));
                if (res.hasBinding("label")) {
                    vDto.setLabel(res.getValue("label").stringValue());
                }
                contexts.add(vDto);
            });
            connection.close();
            return contexts;
        } catch (URISyntaxException e) {
            throw new SGoVException(e);
        }
    }

    /**
     * Finds all vocabularies which are published with optional label in the given language.
     * Each vocabulary is enriched with list of workspaces the vocabulary is in.
     *
     * @param lang language to fetch the label in
     * @return vocabularies in the form of vocabulary context
     */
    public List<VocabularyWithWorkspacesDto> getVocabulariesWithWorkspacesAsDtos(String lang) {
        List<VocabularyWithWorkspacesDto> contexts = new ArrayList<>();
        try {
            final SPARQLRepository repo =
                new SPARQLRepository(IdnUtils.convertUnicodeUrlToAscii(
                    repositoryConf.getUrl()));
            final RepositoryConnection connection = repo.getConnection();
            getVocabulariesAsContextDtos(lang).forEach(vocabularyDto -> {
                final VocabularyWithWorkspacesDto vWDto =
                    new VocabularyWithWorkspacesDto(vocabularyDto);
                connection.prepareTupleQuery("SELECT DISTINCT ?uri ?label WHERE {"
                    + "?uri a <" + Vocabulary.s_c_metadatovy_kontext + "> ;"
                    + " <" + DCTERMS.TITLE + "> ?label ;"
                    + " <" + Vocabulary.s_p_odkazuje_na_kontext + "> ["
                    + "  <" + Vocabulary.s_p_vychazi_z_verze + "> <" + vocabularyDto.getUri() + ">"
                    + " ] . }").evaluate().forEach(res -> {
                        URI wsUri = URI.create(res.getValue("uri").stringValue());
                        String label = res.getValue("label").stringValue();
                        vWDto.addInWorkspace(new WorkspaceDto(wsUri, label));
                    }
                );
                contexts.add(vWDto);
            });
            connection.close();
        } catch (URISyntaxException e) {
            throw new SGoVException(e);
        }
        return contexts;
    }

    /**
     * Returns the list of vocabularies which are write-locked (i.e. they are writable in some
     * workspace).
     */
    private Set<URI> getWriteLockedVocabularies() {
        final Set<URI> result = new HashSet<>();
        workspaceDao.findAll().forEach(w -> w.getAllAttachmentContexts()
            .forEach(vc -> result.add(vc.getBasedOnVersion())));
        return result;
    }

    /**
     * Verifies that given vocabulary is not part of any workspace.
     *
     * @param vocabularyUri Uri of the vocabulary.
     */
    public void verifyVocabularyNotInAnyWorkspace(URI vocabularyUri) {
        this.findAll().stream().filter(
            vc -> vc.getBasedOnVersion().equals(vocabularyUri)
        ).findAny().ifPresent(
            vc -> {
                throw new SGoVException(String.format(
                    "Vocabulary %s already exists in a workspace within context %s.",
                    vocabularyUri,
                    vc.getUri()));
            }
        );
    }

    /**
     * Retrieve vocabulary status, i.e. information whether this vocabulary was
     * published or edited in a workspace.
     *
     * @param vocabularyUri Uri of the vocabulary.
     * @return vocabulary status
     */
    public VocabularyStatusDto getVocabularyStatus(URI vocabularyUri) {
        return new VocabularyStatusDto(
            isVocabularyPublished(vocabularyUri),
            existsInAWorkspace(vocabularyUri)
        );

    }

    /**
     * Verifies that given vocabulary is not published.
     *
     * @param vocabularyUri Uri of the vocabulary.
     */
    public void verifyVocabularyNotPublished(URI vocabularyUri) {
        if (isVocabularyPublished(vocabularyUri)) {
            throw new SGoVException(String.format(
                "Vocabulary %s already exists in a workspace.",
                vocabularyUri));
        }
    }

    /**
     * Tests is given vocabulary exists in a workspace.
     *
     * @param vocabularyUri Uri of the vocabulary.
     * @return True if the vocabulary exists in a workspace.
     */
    private boolean existsInAWorkspace(URI vocabularyUri) {
        return this.findAll().stream().anyMatch(
            vc -> vc.getBasedOnVersion().equals(vocabularyUri)
        );
    }

    /**
     * Tests is given vocabulary is published.
     *
     * @param vocabularyUri Uri of the vocabulary.
     * @return True if the vocabulary is published.
     */
    private boolean isVocabularyPublished(URI vocabularyUri) {
        return getVocabulariesAsContextDtos(null).stream().anyMatch(
            v -> v.getBasedOnVersion().equals(vocabularyUri)
        );
    }

    /**
     * Reloads the given vocabulary context from the source endpoint.
     *
     * @param uri the context to be populated.
     */
    private void populateContext(final String uri,
                                 final Iterable<? extends Statement> statements) {
        final HTTPRepository workspaceRepository = new HTTPRepository(
            repositoryConf.getUrl());
        final RepositoryConnection connection2 = workspaceRepository.getConnection();
        connection2.setParserConfig(
            new ParserConfig().set(BasicParserSettings.PRESERVE_BNODE_IDS, true));

        connection2.begin();
        final ValueFactory f = connection2.getValueFactory();
        connection2.add(statements,
            f.createIRI(uri));
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
        vocabularyDao.persist(vocabularyContext);
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

        populateContext(vocabularyContext.getUri().toString(), statements);
        connection2.close();
    }

    /**
     * Reloads the given vocabulary context from the source endpoint.
     *
     * @param context the vocabulary context to be loaded.
     */
    @Transactional
    public void loadContext(final TrackableContext context) {
        try {
            final SPARQLRepository repo =
                new SPARQLRepository(IdnUtils.convertUnicodeUrlToAscii(
                    repositoryConf.getUrl()));
            try (final RepositoryConnection connection = repo.getConnection()) {
                final String iri = context.getBasedOnVersion().toString();
                populateContext(context.getUri().toString(),
                    loadContext(context, connection));

                if (context instanceof VocabularyContext) {
                    final VocabularyContext vocabularyContext = (VocabularyContext) context;
                    final GraphQueryResult r = loadAttachments(iri, connection);
                    final Set<Statement> set = r.stream().collect(Collectors.toSet());
                    final IRI hasAttachment =
                        repo.getValueFactory().createIRI(Vocabulary.s_p_ma_prilohu);
                    final Set<URI> attachments = set.stream()
                        .filter(s -> s.getPredicate().equals(hasAttachment))
                        .map(s -> URI.create(s.getObject().stringValue()))
                        .collect(Collectors.toSet());
                    vocabularyContext.setAttachments(attachments);
                }
            }
        } catch (URISyntaxException e) {
            throw new SGoVException(e);
        }
    }

    GraphQueryResult loadContext(
        final TrackableContext context,
        final RepositoryConnection connection) {
        final URI version = context.getBasedOnVersion();
        final GraphQuery query = connection
            .prepareGraphQuery("PREFIX : <"
                + version
                + "/> CONSTRUCT {?s ?p ?o} WHERE { GRAPH ?g {?s ?p ?o} FILTER(?g IN (<"
                + version
                + ">" + ((context instanceof VocabularyContext)
                ? ",:glosář,:model,:mapování,:přílohy" : "")
                + "))"
                + "FILTER(?p != <" + Vocabulary.s_p_ma_gestora + ">)}");
        return query.evaluate();
    }

    GraphQueryResult loadAttachments(
        final String vocabularyVersion,
        final RepositoryConnection connection) {
        final GraphQuery query = connection
            .prepareGraphQuery(
                " CONSTRUCT {?s ?p ?o} WHERE { "
                    + "BIND(<" + Vocabulary.s_p_ma_prilohu + "> as ?p) "
                    + "GRAPH <" + vocabularyVersion + "> {?s ?p ?o} }");
        return query.evaluate();
    }

    @Override
    protected VocabularyDao getPrimaryDao() {
        return vocabularyDao;
    }

    @Override
    public void remove(URI id) {
        Optional<VocabularyContext> vocabularyContext = vocabularyDao.find(id);
        if (vocabularyContext.isPresent()) {
            VocabularyContext instance = vocabularyContext.get();
            remove(instance);
        }
    }

    @Override
    public void remove(VocabularyContext instance) {
        removeAllAttachments(instance);
        clearContext(instance.getChangeTrackingContext().getUri());
        vocabularyDao.clearApplicationContexts(instance.getUri());
        super.remove(instance);
        clearContext(instance.getUri());
    }

    public void clearContext(final URI contextUri) {
        vocabularyDao.clearContext(contextUri);
    }

    private void removeAllAttachments(VocabularyContext vocabularyContext) {
        vocabularyContext.getAttachmentContexts().forEach(attachmentRepositoryService::remove);
    }
}
