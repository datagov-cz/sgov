package com.github.sgov.server.service;

import static org.junit.jupiter.api.Assertions.assertEquals;


import com.github.sgov.server.environment.Generator;
import java.net.URI;
import org.junit.jupiter.api.Test;

class IdentifierResolverTest extends BaseServiceTestRunner {

    @Test
    void normalizeTransformsValueToLowerCase() {
        final String value = "CapitalizedSTring";
        assertEquals(value.toLowerCase(), IdentifierResolver.normalize(value));
    }

    @Test
    void normalizeTrimsValue() {
        final String value = "   DDD   ";
        assertEquals(value.trim().toLowerCase(), IdentifierResolver.normalize(value));
    }

    @Test
    void normalizeReplacesSpacesWithDashes() {
        final String value = "Catherine Halsey";
        assertEquals("catherine-halsey", IdentifierResolver.normalize(value));
    }

    @Test
    void normalizeChangesCzechAccutesToAsciiCharacters() {
        final String value = "Strukturální Plán";
        assertEquals("strukturalni-plan", IdentifierResolver.normalize(value));
    }

    @Test
    void normalizeChangesCzechAdornmentsToAsciiCharacters() {
        final String value = "předzahrádka";
        assertEquals("predzahradka", IdentifierResolver.normalize(value));
    }

    @Test
    void normalizeReplacesForwardSlashesWithDashes() {
        final String value = "Slovník vyhlášky č. 500/2006 Sb.";
        assertEquals("slovnik-vyhlasky-c.-500-2006-sb.", IdentifierResolver.normalize(value));
    }

    @Test
    void normalizeReplacesBackwardSlashesWithDashes() {
        final String value = "C:\\Users";
        assertEquals("c:-users", IdentifierResolver.normalize(value));
    }

    @Test
    void normalizeRemovesParentheses() {
        final String value = "Dokument pro Slovník zákona č. 183/2006 Sb. (Stavební zákon)";
        assertEquals("dokument-pro-slovnik-zakona-c.-183-2006-sb.-stavebni-zakon",
            IdentifierResolver.normalize(value));
    }

    @Test
    void normalizeRemovesQueryParameterDelimiters() {
        final String value = "legal-content/SK/TXT/HTML/?uri=CELEX:32010R0996&form=sk";
        assertEquals("legal-content-sk-txt-html-uri=celex:32010r0996form=sk",
            IdentifierResolver.normalize(value));
    }

    @Test
    void generateIdentifierAppendsNormalizedComponentsToNamespaceLoadedFromConfig() {
        final String namespace =
            "https://slovník.gov.cz/uživatel/";
        final String comp = "John Doe";
        final String result = IdentifierResolver.generateUserIdentifier(comp).toString();
        assertEquals(namespace + "john-doe", result);
    }

    @Test
    void resolveIdentifierAppendsFragmentToSpecifiedNamespace() {
        final String namespace = "http://onto.fel.cvut.cz/ontologies/termit/vocabulary/";
        final String fragment = "metropolitan-plan";
        assertEquals(namespace + fragment,
            IdentifierResolver.resolveIdentifier(namespace, fragment).toString());
    }

    @Test
    void resolveIdentifierAppendsSlashAndFragmentIfNamespaceDoesNotEndWithOne() {
        final String namespace = "http://onto.fel.cvut.cz/ontologies/termit/vocabulary";
        final String fragment = "metropolitan-plan";
        assertEquals(namespace + "/" + fragment,
            IdentifierResolver.resolveIdentifier(namespace, fragment).toString());
    }

    @Test
    void resolveIdentifierDoesNotAppendSlashIfNamespaceEndsWithHashTag() {
        final String namespace = "http://onto.fel.cvut.cz/ontologies/termit/vocabulary#";
        final String fragment = "metropolitan-plan";
        assertEquals(namespace + fragment,
            IdentifierResolver.resolveIdentifier(namespace, fragment).toString());
    }

    @Test
    void extractIdentifierFragmentExtractsLastPartOfUri() {
        final URI uri = Generator.generateUri();
        final String result = IdentifierResolver.extractIdentifierFragment(uri);
        assertEquals(uri.toString().substring(uri.toString().lastIndexOf('/') + 1), result);
    }

    @Test
    void extractIdentifierFragmentExtractsFragmentFromUriWithUrlFragment() {
        final URI uri = URI.create("http://onto.fel.cvut.cz/ontologies/termit/vocabulary#test");
        assertEquals("test", IdentifierResolver.extractIdentifierFragment(uri));
    }

    @Test
    void extractIdentifierNamespaceExtractsNamespaceFromSlashBasedUri() {
        final String namespace = "http://onto.fel.cvut.cz/ontologies/termit/vocabulary/";
        final String fragment = "metropolitan-plan";
        final String result =
            IdentifierResolver.extractIdentifierNamespace(URI.create(namespace + fragment));
        assertEquals(namespace, result);
    }

    @Test
    void extractIdentifierNamespaceExtractsNamespaceFromHashBasedUri() {
        final String namespace = "http://onto.fel.cvut.cz/ontologies/termit/vocabulary#";
        final String fragment = "metropolitan-plan";
        final String result =
            IdentifierResolver.extractIdentifierNamespace(URI.create(namespace + fragment));
        assertEquals(namespace, result);
    }
}
