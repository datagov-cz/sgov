package com.github.sgov.server.util;

import com.google.common.io.Files;
import java.io.File;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

public class VocabularyFolderTest {

    @Test
    public void testGetsVocabularyFolderForZSGoVIri() {
        final File f = Files.createTempDir();
        final VocabularyFolder dir =
            VocabularyFolder.ofVocabularyIri(f, new VocabularyInstance("https://slovník.gov.cz/základní"));
        Assert.assertEquals(f.toString() + "/z-sgov", dir.getFolder().toString());
    }
}
