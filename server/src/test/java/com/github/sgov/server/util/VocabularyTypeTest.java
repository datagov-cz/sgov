package com.github.sgov.server.util;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

public class VocabularyTypeTest {

    @Test
    public void allVocabularyTypesResolveVocabularyFoldersCorrectly() {
        Assert.assertEquals("z-sgov",VocabularyType.ZSGOV.getVocabularyFolder(""));
        Assert.assertEquals("v-sgov",VocabularyType.VSGOV.getVocabularyFolder(""));
        Assert.assertEquals("g-sgov/g-sgov-a",VocabularyType.GSGOV.getVocabularyFolder("a"));
        Assert.assertEquals("l-sgov/l-sgov-a",VocabularyType.LSGOV.getVocabularyFolder("a"));
        Assert.assertEquals("a-sgov/a-sgov-a",VocabularyType.ASGOV.getVocabularyFolder("a"));
        Assert.assertEquals("d-sgov/d-sgov-a",VocabularyType.DSGOV.getVocabularyFolder("a"));
    }
}
