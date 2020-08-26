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
public final class VocabularyHelper {

    private static String BIBO_STATUS = "http://purl.org/ontology/bibo/status";

    private static void addCommon(ValueFactory f,
                                  IRI g,
                                  String label,
                                  String namespacePrefix,
                                  String namespaceUri,
                                  Set<Statement> statements) {
        IRI license = f.createIRI("https://creativecommons.org/licenses/by-sa/4.0");
        statements.add(f.createStatement(g, RDF.TYPE, OWL.ONTOLOGY));
        statements.add(f.createStatement(g, DCTERMS.CREATED, f.createLiteral(new Date())));
        statements.add(f.createStatement(g, DCTERMS.RIGHTS, license));
        statements.add(f.createStatement(g, DCTERMS.TITLE, f.createLiteral(label, "cs")));
        statements.add(f.createStatement(g, f.createIRI(BIBO_STATUS),
            f.createLiteral("Specifikace", "cs")));
        statements.add(f.createStatement(g, VANN.PREFERRED_NAMESPACE_PREFIX,
            f.createLiteral(namespacePrefix)));
        statements.add(f.createStatement(g, VANN.PREFERRED_NAMESPACE_URI,
            f.createLiteral(namespaceUri)));
        statements.add(f.createStatement(g, OWL.VERSIONIRI,
            f.createLiteral(g.toString() + "/verze/1.0.0")));
    }

    /**
     * Creates a vocabulary skeleton as RDF4J statements.
     *
     * @param f               RDF4J value factory
     * @param vocabulary      vocabulary IRI
     * @param label           vocabulary label
     * @param statements      set of output statements
     * @param namespacePrefix namespace prefix
     * @return vocabulary resource
     */
    public static IRI createVocabulary(ValueFactory f,
                                       IRI vocabulary,
                                       String label,
                                       Set<Statement> statements,
                                       String namespacePrefix) {
        // glossary
        IRI g = f.createIRI(vocabulary.toString() + "/glosář");
        addCommon(f, g, label + " - glosář", namespacePrefix, vocabulary.toString()
            + "/pojem/", statements);
        statements.add(f.createStatement(g, RDF.TYPE, f.createIRI(Vocabulary.s_c_glosar)));
        statements.add(f.createStatement(g, RDF.TYPE, SKOS.CONCEPT_SCHEME));

        // model
        IRI m = f.createIRI(vocabulary.toString() + "/model");
        addCommon(f, m, label + " - model", namespacePrefix, vocabulary.toString()
            + "/pojem/", statements);
        statements.add(f.createStatement(m, OWL.IMPORTS, g));
        statements.add(f.createStatement(m, RDF.TYPE, f.createIRI(Vocabulary.s_c_model)));

        // slovnik
        IRI s = f.createIRI(vocabulary.toString());
        addCommon(f, s, label, namespacePrefix, vocabulary.toString()
            + "/pojem/", statements);
        statements.add(f.createStatement(s, RDF.TYPE, f.createIRI(Vocabulary.s_c_slovnik)));
        statements.add(f.createStatement(s, OWL.IMPORTS, g));
        statements.add(f.createStatement(s, OWL.IMPORTS, m));
        statements.add(f.createStatement(s, f.createIRI(Vocabulary.s_p_ma_glosar), g));
        statements.add(f.createStatement(s, f.createIRI(Vocabulary.s_p_ma_model), m));

        return s;
    }

    private static String getId(String iri, String iriPrefix) {
        return iri.substring(iriPrefix
            .length() + 1).replace("/", "-") + "-pojem";
    }

    /**
     * Gets vocabulary prefix for URI.
     *
     * @param vocabularyIri URI of vocabulary
     * @return resulting prefix
     */
    public static String getPrefix(final String vocabularyIri) {
        if (vocabularyIri.startsWith(Vocabulary.SLOVNIK_GOV_CZ + "/základní")) {
            return "z-sgov-pojem";
        } else if (vocabularyIri.startsWith(Vocabulary.SLOVNIK_GOV_CZ + "/veřejný-sektor")) {
            return "v-sgov-pojem";
        } else if (vocabularyIri.startsWith(Vocabulary.SLOVNIK_GOV_CZ + "/generický")) {
            return "g-sgov-" + getId(vocabularyIri, Vocabulary.SLOVNIK_GOV_CZ + "/generický");
        } else if (vocabularyIri.startsWith(Vocabulary.SLOVNIK_GOV_CZ + "/legislativní")) {
            return "l-sgov-" + getId(vocabularyIri, Vocabulary.SLOVNIK_GOV_CZ + "/legislativní");
        } else if (vocabularyIri.startsWith(Vocabulary.SLOVNIK_GOV_CZ + "/agendový")) {
            return "a-sgov-" + getId(vocabularyIri, Vocabulary.SLOVNIK_GOV_CZ + "/agendový");
        } else if (vocabularyIri.startsWith(Vocabulary.SLOVNIK_GOV_CZ + "/datový")) {
            return "d-sgov-" + getId(vocabularyIri, Vocabulary.SLOVNIK_GOV_CZ + "/datový");
        } else {
            throw new IllegalArgumentException();
        }
    }
}