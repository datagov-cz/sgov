package com.github.sgov.server.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class VocabularyFolderTest {

    @Test
    public void testGetsVocabularyFolderForZSGoVIri() throws IOException {
        final File f = Files.createTempDirectory(getClass().getName()).toFile();
        final VocabularyFolder dir =
            VocabularyFolder.ofVocabularyIri(f, new VocabularyInstance("https://slovník.gov.cz/základní"));
        Assertions.assertEquals(f + "/content/z-sgov", dir.getFolder().toString());
    }

    @Test
    public void testGetsVocabularyFolderForVSGoVIri() throws IOException {
        final File f = Files.createTempDirectory(getClass().getName()).toFile();
        final VocabularyFolder dir =
            VocabularyFolder.ofVocabularyIri(f, new VocabularyInstance("https://slovník.gov.cz/veřejný-sektor"));
        Assertions.assertEquals(f + "/content/v-sgov", dir.getFolder().toString());
    }
}
