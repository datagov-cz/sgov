package com.github.sgov.server.util;

import com.google.common.io.Files;
import java.io.File;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class VocabularyFolderTest {

    @Test
    public void testGetsVocabularyFolderForZSGoVIri() {
        final File f = Files.createTempDir();
        final VocabularyFolder dir =
            VocabularyFolder.ofVocabularyIri(f, new VocabularyInstance("https://slovník.gov.cz/základní"));
        Assertions.assertEquals(f + "/content/z-sgov", dir.getFolder().toString());
    }

    @Test
    public void testGetsVocabularyFolderForVSGoVIri() {
        final File f = Files.createTempDir();
        final VocabularyFolder dir =
            VocabularyFolder.ofVocabularyIri(f, new VocabularyInstance("https://slovník.gov.cz/veřejný-sektor"));
        Assertions.assertEquals(f + "/content/v-sgov", dir.getFolder().toString());
    }
}
