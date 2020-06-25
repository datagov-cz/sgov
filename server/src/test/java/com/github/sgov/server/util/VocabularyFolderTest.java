package com.github.sgov.server.util;

import com.google.common.io.Files;
import java.io.File;
import java.net.URI;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

public class VocabularyFolderTest {

    @Test
    public void testGetsNoVocabularyFolderForInvalidIri() {
        File f = Files.createTempDir();
        VocabularyFolder dir = VocabularyFolder.ofVocabularyIri(f, URI.create(""));
        Assert.assertEquals(null, dir);
    }

    @Test
    public void testGetsZSGoVVocabularyFolderForUnknownVocabularyType() {
        final File f = Files.createTempDir();
        final VocabularyFolder dir =
            VocabularyFolder.ofVocabularyIri(f, URI.create("https://slovník.gov.cz/x"));
        Assert.assertEquals(null, dir);
    }

    @Test
    public void testGetsVocabularyFolderForZSGoVIri() {
        final File f = Files.createTempDir();
        final VocabularyFolder dir =
            VocabularyFolder.ofVocabularyIri(f, URI.create("https://slovník.gov.cz/základní"));
        Assert.assertEquals(f.toString() + "/z-sgov", dir.getFolder().toString());
    }

    @Test
    public void testGetsVocabularyFolderForVSGoVIri() {
        final File f = Files.createTempDir();
        final VocabularyFolder dir = VocabularyFolder
            .ofVocabularyIri(f, URI.create("https://slovník.gov.cz/veřejný-sektor"));
        Assert.assertEquals(f.toString() + "/v-sgov", dir.getFolder().toString());
    }

    @Test
    public void testGetsVocabularyFolderForGSGoVIri() {
        final File f = Files.createTempDir();
        final VocabularyFolder dir = VocabularyFolder
            .ofVocabularyIri(f, URI.create("https://slovník.gov.cz/generický/aktuality"));
        Assert.assertEquals(f.toString()
            + "/g-sgov/g-sgov-aktuality", dir.getFolder().toString());
    }

    @Test
    public void testGetsVocabularyFolderForLSGoVIri() {
        final File f = Files.createTempDir();
        final VocabularyFolder dir = VocabularyFolder
            .ofVocabularyIri(f, URI.create("https://slovník.gov.cz/legislativní/sbírka/111/2009"));
        Assert.assertEquals(f.toString() + "/l-sgov/l-sgov-sbírka-111-2009",
            dir.getFolder().toString());
    }

    @Test
    public void testGetsVocabularyFolderForASGoVIri() {
        final File f = Files.createTempDir();
        final VocabularyFolder dir =
            VocabularyFolder.ofVocabularyIri(f, URI.create("https://slovník.gov.cz/agendový/104"));
        Assert.assertEquals(f.toString()
            + "/a-sgov/a-sgov-104", dir.getFolder().toString());
    }

    @Test
    public void testGetsVocabularyFolderForDSGoVIri() {
        final File f = Files.createTempDir();
        final VocabularyFolder dir =
            VocabularyFolder
                .ofVocabularyIri(f, URI.create("https://slovník.gov.cz/datový/pracovní-prostor"));
        Assert.assertEquals(f.toString() + "/d-sgov/d-sgov-pracovní-prostor",
            dir.getFolder().toString());
    }
}
