package com.github.sgov.server.service.repository;

import static com.github.sgov.server.util.Vocabulary.BIBO_NAMESPACE;
import static com.github.sgov.server.util.Vocabulary.DATA_DESCRIPTION_NAMESPACE;
import static com.github.sgov.server.util.Vocabulary.SLOVNIK_GOV_CZ;
import static com.github.sgov.server.util.Vocabulary.VANN_NAMESPACE;

import com.github.sgov.server.config.conf.RepositoryConf;
import com.github.sgov.server.exception.SGoVException;
import com.github.sgov.server.model.AttachmentContext;
import com.github.sgov.server.model.VocabularyContext;
import com.github.sgov.server.util.AttachmentFolder;
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

    public static final String NS_TERMIT = "http://onto.fel.cvut.cz/ontologies/application/termit";
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
            || statement.getPredicate().getNamespace().equals(SKOS.NAMESPACE)
            || statement.getPredicate().getNamespace().equals(DCTERMS.NAMESPACE);
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
            final String changeTrackingUrl = context.getChangeTrackingContext().getUri().toString();

            final IRI ctxWorkspaceEntity =
                cWorkspaceRepo.getValueFactory().createIRI(context.getUri().toString());

            final RepositoryConnection conGitSsp = getSsp();

            final ValueFactory fsspRepo = conGitSsp.getValueFactory();
            final IRI ctxVocabulary = fsspRepo.createIRI(versionUrl);
            final IRI ctxIri = fsspRepo.createIRI(context.getUri().toString());
            final IRI ctxChangeTracking = fsspRepo.createIRI(changeTrackingUrl);

            final String vocabularyId = Utils.getVocabularyId(versionUrl);
            conGitSsp.setNamespace(vocabularyId + "-pojem",
                ctxVocabulary.toString() + "/pojem/");
            conGitSsp.setNamespace(vocabularyId, ctxVocabulary + "/");

            final IRI ctxModel = fsspRepo.createIRI(versionUrl + "/model");
            final IRI ctxGlossary = fsspRepo.createIRI(versionUrl + "/glosář");

            final IRI hasAttachment = fsspRepo.createIRI(Vocabulary.s_p_ma_prilohu);
            final IRI ctxAttachments = fsspRepo.createIRI(versionUrl + "/přílohy");

            cWorkspaceRepo.getStatements(null, null, null, ctxWorkspaceEntity)
                .stream()
                .forEach(s -> {
                    // do not publish information about the contexts
                    if (!s.getSubject().equals(ctxIri)
                        && !s.getSubject().equals(ctxChangeTracking)) {
                        if (s.getSubject().equals(ctxVocabulary)) {
                            if (s.getPredicate().equals(hasAttachment)) {
                                // do not add old ones. They should reflect the current change.
                            } else {
                                conGitSsp.add(s, ctxVocabulary);
                            }
                        } else if (s.getSubject().equals(ctxModel)) {
                            conGitSsp.add(s, ctxModel);
                        } else if (s.getSubject().equals(ctxGlossary)) {
                            conGitSsp.add(s, ctxGlossary);
                        } else {
                            conGitSsp.add(s, isGlossaryTriple(s) ? ctxGlossary : ctxModel);
                        }
                    }
                });

            context.getAttachments().forEach(attachment ->
                conGitSsp.add(ctxVocabulary, hasAttachment,
                    fsspRepo.createIRI(attachment.toString()), ctxAttachments)
            );

            folder.getFolder().mkdirs();

            saveContextToFile(conGitSsp, ctxVocabulary, folder.getVocabularyFile());
            saveContextToFile(conGitSsp, ctxGlossary, folder.getGlossaryFile());
            saveContextToFile(conGitSsp, ctxModel, folder.getModelFile());
            if (! context.getAttachments().isEmpty()) {
                saveContextToFile(conGitSsp, ctxAttachments, folder.getAttachmentsFile());
            }

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
    public void storeContext(final AttachmentContext context,
                             final AttachmentFolder folder) {
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
            final IRI ctxAttachment = fsspRepo.createIRI(versionUrl);
            final IRI ctxIri = fsspRepo.createIRI(context.getUri().toString());
            conGitSsp.setNamespace(Utils.getAttachmentId(versionUrl), ctxAttachment + "/");

            cWorkspaceRepo.getStatements(null, null, null, ctxWorkspaceEntity)
                .forEach(s -> {
                    // do not publish information about the contexts
                    if (!s.getSubject().equals(ctxIri)) {
                        conGitSsp.add(s, ctxAttachment);
                    }
                });

            folder.getFolder().mkdirs();

            final File file = folder.getAttachmentFile();
            conGitSsp.export(getDeterministicWriter(new FileWriter(file)), ctxAttachment);

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

    private void saveContextToFile(
                                   RepositoryConnection inputRdf4RepositoryConnection,
                                   IRI inputDataContext,
                                   File outputFile
                                   ) throws IOException {
        inputRdf4RepositoryConnection.export(
            getDeterministicWriter(new FileWriter(outputFile)),
            inputDataContext);
    }
}
