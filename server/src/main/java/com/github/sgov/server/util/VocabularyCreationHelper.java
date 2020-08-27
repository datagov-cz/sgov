package com.github.sgov.server.util;

import java.util.Date;
import java.util.Set;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.model.vocabulary.VANN;

/**
 * Vocabulary for the SGoV server model.
 */
public final class VocabularyCreationHelper {

    private static String BIBO_STATUS = "http://purl.org/ontology/bibo/status";

    private static void addCommon(ValueFactory f,
                                  IRI iri,
                                  VocabularyInstance vocabulary,
                                  String label,
                                  Set<Statement> statements) {
        IRI license = f.createIRI("https://creativecommons.org/licenses/by-sa/4.0");
        statements.add(f.createStatement(iri, RDF.TYPE, OWL.ONTOLOGY));
        statements.add(f.createStatement(iri, DCTERMS.CREATED, f.createLiteral(new Date())));
        statements.add(f.createStatement(iri, DCTERMS.RIGHTS, license));
        statements.add(f.createStatement(iri, DCTERMS.TITLE, f.createLiteral(label, "cs")));
        statements.add(f.createStatement(iri, f.createIRI(BIBO_STATUS),
            f.createLiteral("Specifikace", "cs")));
        statements.add(f.createStatement(iri, VANN.PREFERRED_NAMESPACE_PREFIX,
            f.createLiteral(vocabulary.getPrefix())));
        statements.add(f.createStatement(iri, VANN.PREFERRED_NAMESPACE_URI,
            f.createLiteral(vocabulary.getConceptNamespace())));
        statements.add(f.createStatement(iri, OWL.VERSIONIRI,
            f.createLiteral(iri.toString() + "/verze/1.0.0")));
    }

    /**
     * Creates a vocabulary skeleton as RDF4J statements.
     *
     * @param f               RDF4J value factory
     * @param vocabulary        vocabulary vocabulary
     * @param label           vocabulary label
     * @param statements      set of output statements
     * @return vocabulary resource
     */
    public static IRI createVocabulary(ValueFactory f,
                                       VocabularyInstance vocabulary,
                                       String label,
                                       Set<Statement> statements) {
        String iri = vocabulary.getIri();

        // glossary
        IRI g = f.createIRI(iri + "/glosář");
        addCommon(f, g, vocabulary, label + " - glosář", statements);
        statements.add(f.createStatement(g, RDF.TYPE, f.createIRI(Vocabulary.s_c_glosar)));
        statements.add(f.createStatement(g, RDF.TYPE, SKOS.CONCEPT_SCHEME));

        // model
        IRI m = f.createIRI(iri + "/model");
        addCommon(f, m, vocabulary, label + " - model", statements);
        statements.add(f.createStatement(m, OWL.IMPORTS, g));
        statements.add(f.createStatement(m, RDF.TYPE, f.createIRI(Vocabulary.s_c_model)));

        // slovnik
        IRI s = f.createIRI(iri);
        addCommon(f, s, vocabulary, label, statements);
        statements.add(f.createStatement(s, RDF.TYPE, f.createIRI(Vocabulary.s_c_slovnik)));
        statements.add(f.createStatement(s, OWL.IMPORTS, g));
        statements.add(f.createStatement(s, OWL.IMPORTS, m));
        statements.add(f.createStatement(s, f.createIRI(Vocabulary.s_p_ma_glosar), g));
        statements.add(f.createStatement(s, f.createIRI(Vocabulary.s_p_ma_model), m));

        return s;
    }
}