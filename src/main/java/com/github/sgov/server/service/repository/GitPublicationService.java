package com.github.sgov.server.service.repository;

import static com.github.sgov.server.util.Vocabulary.BIBO_NAMESPACE;
import static com.github.sgov.server.util.Vocabulary.DATA_DESCRIPTION_NAMESPACE;
import static com.github.sgov.server.util.Vocabulary.SLOVNIK_GOV_CZ;
import static com.github.sgov.server.util.Vocabulary.VANN_NAMESPACE;

import com.github.sgov.server.config.conf.RepositoryConf;
import com.github.sgov.server.exception.SGoVException;
import com.github.sgov.server.model.AssetContext;
import com.github.sgov.server.model.VocabularyContext;
import com.github.sgov.server.util.AssetFolder;
import com.github.sgov.server.util.IdnUtils;
import com.github.sgov.server.util.Utils;
import com.github.sgov.server.util.Vocabulary;
import com.github.sgov.server.util.VocabularyFolder;
import com.github.sgov.server.util.VocabularyType;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URISyntaxException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.WriterConfig;
import org.eclipse.rdf4j.rio.helpers.BasicWriterSettings;
import org.eclipse.rdf4j.rio.turtle.ArrangedWriter;
import org.eclipse.rdf4j.rio.turtle.TurtleWriter;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service to managed workspaces.
 */
@Service
public class GitPublicationService {

    private final RepositoryConf repositoryConf;

    /**
     * Creates a new repository service.
     */
    @Autowired
    public GitPublicationService(final RepositoryConf repositoryConf) {
        this.repositoryConf = repositoryConf;
    }

    /**
     * Returns a writer which outputs the same Model in the same form all the time.
     *
     * @param w writer to wrap
     * @return deterministic RDFWriter
     */
    private RDFWriter getDeterministicWriter(final Writer w) {
        final RDFWriter writer = new ArrangedWriter(
            new TurtleWriter(w), 100);
        writer.setWriterConfig(new WriterConfig()
            .set(BasicWriterSettings.PRETTY_PRINT, true)
            .set(BasicWriterSettings.INLINE_BLANK_NODES, true));
        return writer;
    }

    private void addNamespaces(final RepositoryConnection conGitSsp) {
        conGitSsp.setNamespace("rdf", RDF.NAMESPACE);
        conGitSsp.setNamespace("owl", OWL.NAMESPACE);
        conGitSsp.setNamespace("rdfs", RDFS.NAMESPACE);
        conGitSsp.setNamespace("dcterms", DCTERMS.NAMESPACE);
        conGitSsp.setNamespace("xsd", XSD.NAMESPACE);
        conGitSsp.setNamespace("bibo", BIBO_NAMESPACE);
        conGitSsp.setNamespace("vann", VANN_NAMESPACE);
        final String zSGoV = SLOVNIK_GOV_CZ + VocabularyType.ZSGOV.getIriLocalName();
        conGitSsp.setNamespace(VocabularyType.ZSGOV.getPrefix(), zSGoV + "/");
        conGitSsp.setNamespace(VocabularyType.ZSGOV.getPrefix() + "-pojem", zSGoV + "/pojem/");
        conGitSsp.setNamespace("a-popis-dat-pojem", DATA_DESCRIPTION_NAMESPACE);
        conGitSsp.setNamespace("skos", SKOS.NAMESPACE);
    }

    /**
     * Decides whether a statement belongs to a glossary or to a model. All statements with
     * predicate or object in SKOS namespace are considered glossary triples.
     *
     * @param statement statement statement to check
     * @return true if the statement should be put to glossary, false otherwise
     */
    private boolean isGlossaryTriple(final Statement statement) {
        return ((statement.getObject() instanceof IRI)
            && ((IRI) statement.getObject()).getNamespace().equals(SKOS.NAMESPACE))
            || statement.getPredicate().getNamespace().equals(SKOS.NAMESPACE);
    }

    /**
     * Stores the given vocabulary context into the given vocabulary folder.
     *
     * @param context the context to be store.
     * @param folder  folder to store the context into.
     */
    @Transactional
    public void storeContext(final VocabularyContext context,
                             final VocabularyFolder folder) {
        try {
            final SPARQLRepository workspaceRepo =
                new SPARQLRepository(IdnUtils.convertUnicodeUrlToAscii(
                    repositoryConf.getUrl()));
            final RepositoryConnection cWorkspaceRepo = workspaceRepo.getConnection();

            final String versionUrl =
                context.getBasedOnVersion().toString();

            final IRI ctxWorkspaceEntity =
                cWorkspaceRepo.getValueFactory().createIRI(context.getUri().toString());

            final RepositoryConnection conGitSsp = getSsp();

            final ValueFactory fsspRepo = conGitSsp.getValueFactory();
            final IRI ctxVocabulary = fsspRepo.createIRI(versionUrl);

            final String vocabularyId = Utils.getVocabularyId(versionUrl);
            conGitSsp.setNamespace(vocabularyId + "-pojem",
                ctxVocabulary.toString() + "/pojem/");
            conGitSsp.setNamespace(vocabularyId, ctxVocabulary + "/");

            cWorkspaceRepo.getStatements(ctxVocabulary, null, null, ctxWorkspaceEntity)
                .forEach(s -> conGitSsp.add(s, ctxVocabulary));

            final IRI ctxGlossary = fsspRepo.createIRI(versionUrl + "/glosář");
            cWorkspaceRepo.getStatements(ctxGlossary, null, null, ctxWorkspaceEntity)
                .forEach(s -> conGitSsp.add(s, ctxGlossary));

            final IRI ctxModel = fsspRepo.createIRI(versionUrl + "/model");
            cWorkspaceRepo.getStatements(ctxModel, null, null, ctxWorkspaceEntity)
                .forEach(s -> conGitSsp.add(s, ctxModel));

            final IRI maAsset = fsspRepo.createIRI(Vocabulary.s_p_ma_prilohu);
            final IRI ctxAssets = fsspRepo.createIRI(versionUrl + "/přílohy");
            context.getAssets().forEach(asset ->
                conGitSsp.add(ctxVocabulary, maAsset, fsspRepo.createIRI(asset.toString()),
                    ctxAssets)
            );

            cWorkspaceRepo.getStatements(null, null, null, ctxWorkspaceEntity)
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

            folder.getFolder().mkdirs();

            conGitSsp.export(getDeterministicWriter(new FileWriter(folder.getVocabularyFile())),
                ctxVocabulary);
            conGitSsp.export(getDeterministicWriter(new FileWriter(folder.getGlossaryFile())),
                ctxGlossary);
            conGitSsp.export(getDeterministicWriter(new FileWriter(folder.getModelFile())),
                ctxModel);
            conGitSsp.export(getDeterministicWriter(new FileWriter(folder.getAssetsFile())),
                ctxAssets);

            conGitSsp.close();
            cWorkspaceRepo.close();
        } catch (URISyntaxException | IOException e) {
            throw new SGoVException(e);
        }
    }

    /**
     * Stores the given asset context into the given asset folder.
     *
     * @param context the context to be store.
     * @param folder  folder to store the context into.
     */
    @Transactional
    public void storeContext(final AssetContext context,
                             final AssetFolder folder) {
        try {
            final SPARQLRepository workspaceRepo =
                new SPARQLRepository(IdnUtils.convertUnicodeUrlToAscii(
                    repositoryConf.getUrl()));
            final RepositoryConnection cWorkspaceRepo = workspaceRepo.getConnection();

            final String versionUrl =
                context.getBasedOnVersion().toString();

            final IRI ctxWorkspaceEntity =
                cWorkspaceRepo.getValueFactory().createIRI(context.getUri().toString());

            final RepositoryConnection conGitSsp = getSsp();

            final ValueFactory fsspRepo = conGitSsp.getValueFactory();
            final IRI ctxAsset = fsspRepo.createIRI(versionUrl);
            conGitSsp.setNamespace(Utils.getAssetId(versionUrl), ctxAsset + "/");

            cWorkspaceRepo.getStatements(null, null, null, ctxWorkspaceEntity)
                .forEach(s -> conGitSsp.add(s, ctxAsset));

            folder.getFolder().mkdirs();

            final File file = folder.getAssetFile();
            conGitSsp.export(getDeterministicWriter(new FileWriter(file)), ctxAsset);

            conGitSsp.close();
            cWorkspaceRepo.close();
        } catch (final URISyntaxException | IOException e) {
            throw new SGoVException(e);
        }
    }

    private RepositoryConnection getSsp() {
        final MemoryStore sspStore = new MemoryStore();
        final Repository sspRepo = new SailRepository(sspStore);
        final RepositoryConnection conGitSsp = sspRepo.getConnection();

        addNamespaces(conGitSsp);

        return conGitSsp;
    }
}
