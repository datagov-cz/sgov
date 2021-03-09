package com.github.sgov.server.service.repository;

import com.github.sgov.server.service.BaseServiceTestRunner;
import java.io.StringWriter;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class,
    classes = {VocabularyService.class})
class VocabularyRepositoryServiceTest extends BaseServiceTestRunner {

    @Autowired
    private VocabularyService sut;

    @Test
    void getWriterReturnsDeterministicWriter() {
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

        Assert.assertEquals(sw1.toString(), sw2.toString());
    }
}