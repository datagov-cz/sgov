package com.github.sgov.server.util;

import java.io.File;
import lombok.Getter;

@Getter
public class GitLayout {

    private static final String CONTENT_ROOT = "content";


    public static final String getVocabularyFolder(final String iri) {
        return CONTENT_ROOT + "/" + Utils.getVocabularyId(iri);
    }

    public static final String getAssetFolder() {
        return CONTENT_ROOT + "/assets";
    }
}
