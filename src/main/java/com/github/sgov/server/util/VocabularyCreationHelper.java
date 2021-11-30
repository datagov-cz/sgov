package com.github.sgov.server.util;

import static com.github.sgov.server.util.Constants.CC_BY_SA_4;

import com.github.sgov.server.controller.dto.VocabularyContextDto;
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


    private static void addCommon(final ValueFactory f,
                                  final IRI iri,
                                  final String conceptPrefix,
                                  final String conceptNamespace,
                                  final String label,
                                  final Set<Statement> statements,
                                  final String language) {
        statements.add(f.createStatement(iri, RDF.TYPE, OWL.ONTOLOGY));
        statements.add(f.createStatement(iri, DCTERMS.CREATED, f.createLiteral(new Date())));
        statements.add(f.createStatement(iri, DCTERMS.RIGHTS, f.createIRI(CC_BY_SA_4)));
        statements.add(f.createStatement(iri, DCTERMS.TITLE, f.createLiteral(label, language)));
        statements.add(f.createStatement(iri, VANN.PREFERRED_NAMESPACE_PREFIX,
            f.createLiteral(conceptPrefix)));
        statements.add(f.createStatement(iri, VANN.PREFERRED_NAMESPACE_URI,
            f.createLiteral(conceptNamespace)));
        statements.add(f.createStatement(iri, OWL.VERSIONIRI,
            f.createLiteral(iri.toString() + "/verze/1.0.0")));
    }

    /**
     * Creates a vocabulary skeleton as RDF4J statements.
     *
     * @param f          RDF4J value factory
     * @param vocabularyIri vocabulary IRI
     * @param vocabularyContextDto  vocabulary context data
     * @param statements set of output statements
     */
    public static void createVocabulary(ValueFactory f,
                                       String vocabularyIri,
                                       VocabularyContextDto vocabularyContextDto,
                                       Set<Statement> statements, final String language) {
        final String label = vocabularyContextDto.getLabel();
        final String conceptPrefix = Utils.getVocabularyId(vocabularyIri) + "-pojem";
        final String conceptNamespace = vocabularyIri + "/pojem/";

        // glossary
        final IRI g = f.createIRI(vocabularyIri + "/glosář");
        addCommon(f, g, conceptPrefix, conceptNamespace, label + " - glosář", statements, language);
        statements.add(f.createStatement(g, RDF.TYPE, f.createIRI(Vocabulary.s_c_glosar)));
        statements.add(f.createStatement(g, RDF.TYPE, SKOS.CONCEPT_SCHEME));

        // model
        final IRI m = f.createIRI(vocabularyIri + "/model");
        addCommon(f, m, conceptPrefix, conceptNamespace, label + " - model", statements, language);
        statements.add(f.createStatement(m, OWL.IMPORTS, g));
        statements.add(f.createStatement(m, RDF.TYPE, f.createIRI(Vocabulary.s_c_model)));

        // slovnik
        final IRI s = f.createIRI(vocabularyIri);
        addCommon(f, s, conceptPrefix, conceptNamespace, label, statements, language);
        statements.add(f.createStatement(s, RDF.TYPE, f.createIRI(Vocabulary.s_c_slovnik)));
        statements.add(f.createStatement(s, OWL.IMPORTS, g));
        statements.add(f.createStatement(s, OWL.IMPORTS, m));
        statements.add(f.createStatement(s, f.createIRI(Vocabulary.s_p_ma_glosar), g));
        statements.add(f.createStatement(s, f.createIRI(Vocabulary.s_p_ma_model), m));

        final String description = vocabularyContextDto.getDescription();
        if (description != null) {
            statements.add(f.createStatement(s, DCTERMS.DESCRIPTION,
                f.createLiteral(description, language)));
        }
    }
}