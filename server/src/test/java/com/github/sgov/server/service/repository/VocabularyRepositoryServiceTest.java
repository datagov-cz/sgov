package com.github.sgov.server.service.repository;

import com.github.sgov.server.config.conf.RepositoryConf;
import com.github.sgov.server.model.VocabularyContext;
import com.github.sgov.server.service.BaseServiceTestRunner;
import com.github.sgov.server.util.Vocabulary;
import com.github.sgov.server.util.VocabularyType;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(initializers = ConfigFileApplicationContextInitializer.class,
    classes = {VocabularyService.class})
class VocabularyRepositoryServiceTest extends BaseServiceTestRunner {

    @Autowired
    private VocabularyService sut;

    @Autowired
    private RepositoryConf repositoryConf;

    private FusekiServer server;

    private void setUp(Dataset ds) {
        server = FusekiServer.create()
            .port(1234)
            .add("", ds)
            .build();
        server.start();
        repositoryConf.setReleaseSparqlEndpointUrl("http://localhost:1234/");
        MockitoAnnotations.initMocks(this);
    }

    private void tearDown() {
        server.stop();
    }

    private Dataset createDatasetOfVocabularyStubs(String... iris) {
        final Dataset ds = DatasetFactory.create();

        Arrays.stream(iris).forEach(iri -> {
            Model model = ModelFactory.createDefaultModel();
            final Resource voc = model.createResource(iri);
            model.add(voc, RDF.type, model.createResource(Vocabulary.s_c_slovnik));
            model.add(voc, DCTerms.title, model.createLiteral("Slovník " + iri, "cs"));
            ds.addNamedModel(voc.getURI(), model);
        });
        return ds;
    }

    @Test
    void getVocabulariesAsContextDtosReturnsAllIncludingZSGoV() {
        final String[] vocabularies = new String[]{VocabularyType.ZSGOV.getVocabularyPattern(),
            VocabularyType.VSGOV.getVocabularyPattern()};
        final Dataset ds =
            createDatasetOfVocabularyStubs(vocabularies);
        setUp(ds);

        final List<VocabularyContext> contexts = sut.getVocabulariesAsContextDtos();
        Assert.assertEquals(2, contexts.size());

        Assert.assertEquals(new HashSet<>(Arrays.asList(vocabularies)),
            contexts.stream().map(c ->
                c.getBasedOnVocabularyVersion().toString()).collect(Collectors.toSet())
        );

        tearDown();
    }

    @Test
    void getWriterReturnsDeterministicWriter() throws IOException {
        final MemoryStore sspStore = new MemoryStore();
        final Repository sspRepo = new SailRepository(sspStore);
        final RepositoryConnection conGitSsp = sspRepo.getConnection();

        final ValueFactory fsspRepo = conGitSsp.getValueFactory();
        final IRI a = fsspRepo.createIRI("https://example.org/a");
        final IRI b = fsspRepo.createIRI("https://example.org/b");

        final BNode bnode = fsspRepo.createBNode();
        final BNode bnode2 = fsspRepo.createBNode();

        conGitSsp.add(a, RDFS.SUBCLASSOF, bnode);
        conGitSsp.add(bnode, org.eclipse.rdf4j.model.vocabulary.OWL.ONPROPERTY, b);
        conGitSsp.add(bnode, OWL.ALLVALUESFROM, b);
        conGitSsp.add(a, RDFS.SUBCLASSOF, bnode2);
        conGitSsp.add(bnode2, org.eclipse.rdf4j.model.vocabulary.OWL.ONPROPERTY, b);
        conGitSsp.add(bnode2, OWL.SOMEVALUESFROM, b);

        final StringWriter sw1 = new StringWriter();
        final RDFWriter w1 = sut.getDeterministicWriter(sw1);
        conGitSsp.export(w1);

        final StringWriter sw2 = new StringWriter();
        final RDFWriter w2 = sut.getDeterministicWriter(sw2);
        conGitSsp.export(w2);

        Assert.assertEquals(sw1.toString(),sw2.toString());
    }
}