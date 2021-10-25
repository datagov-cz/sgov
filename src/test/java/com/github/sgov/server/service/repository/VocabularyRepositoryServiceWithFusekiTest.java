package com.github.sgov.server.service.repository;

import com.github.sgov.server.config.conf.RepositoryConf;
import com.github.sgov.server.controller.dto.VocabularyDto;
import com.github.sgov.server.service.BaseServiceTestRunner;
import com.github.sgov.server.util.Vocabulary;
import com.github.sgov.server.util.VocabularyType;
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class,
    classes = {VocabularyService.class})
class VocabularyRepositoryServiceWithFusekiTest extends BaseServiceTestRunner {

    @Autowired
    private VocabularyService sut;

    @Autowired
    private RepositoryConf repositoryConf;

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
        final String[] vocabularies = new String[] {VocabularyType.ZSGOV.getVocabularyPattern(),
            VocabularyType.VSGOV.getVocabularyPattern()};
        final Dataset ds =
            createDatasetOfVocabularyStubs(vocabularies);
        final FusekiServer server = FusekiServer.create()
            .port(1234)
            .add("", ds)
            .build();
        server.start();
        repositoryConf.setReleaseSparqlEndpointUrl("http://localhost:1234/");

        final List<VocabularyDto> contexts = sut.getVocabulariesAsContextDtos();
        Assertions.assertEquals(2, contexts.size());

        Assertions.assertEquals(new HashSet<>(Arrays.asList(vocabularies)),
            contexts.stream().map(c ->
                c.getBasedOnVocabularyVersion().toString()).collect(Collectors.toSet())
        );

        server.stop();
    }
}