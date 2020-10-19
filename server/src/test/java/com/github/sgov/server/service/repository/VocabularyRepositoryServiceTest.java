package com.github.sgov.server.service.repository;

import com.github.sgov.server.config.conf.RepositoryConf;
import com.github.sgov.server.dao.VocabularyDao;
import com.github.sgov.server.dao.WorkspaceDao;
import com.github.sgov.server.model.VocabularyContext;
import com.github.sgov.server.model.Workspace;
import com.github.sgov.server.service.BaseServiceTestRunner;
import com.github.sgov.server.util.Vocabulary;
import com.github.sgov.server.util.VocabularyType;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
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
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
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

    @Mock
    private VocabularyDao vocabularyDao;

    @Mock
    private WorkspaceDao workspaceDao;

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
        final Dataset ds =
            createDatasetOfVocabularyStubs(VocabularyType.ZSGOV.getVocabularyPattern(),
                VocabularyType.VSGOV.getVocabularyPattern());
        setUp(ds);

        final List<VocabularyContext> contexts = sut.getVocabulariesAsContextDtos();
        Assert.assertEquals(2, contexts.size());
        Assert.assertEquals(Collections.singleton(VocabularyType.VSGOV.getVocabularyPattern()),
            contexts.stream().map(c ->
                c.getBasedOnVocabularyVersion().toString()).collect(Collectors.toSet())
        );

        tearDown();
    }
}