package com.github.sgov.server.util;

import java.io.File;

public class VocabularyFolder extends Folder {

    public VocabularyFolder(File folder) {
        super(folder);
    }

    public File getVocabularyFile() {
        return getFile("slovník", "");
    }

    public File getGlossaryFile() {
        return getFile("glosář", "");
    }

    public File getModelFile() {
        return getFile("model", "");
    }

    public File getAttachmentsFile() {
        return getFile("přílohy", "");
    }

    /**
     * Return all non-compact vocabulary files - to be deleted upon update.
     *
     * @return array of files
     */
    public File[] toPruneAllExceptCompact() {
        return getFolder().listFiles((file, s) -> (
            s.contains("-model")
                || s.contains("-glosář")
                || s.contains("-slovník")
                || s.contains("-přílohy")
        ) && !s.endsWith("-compact.ttl"));
    }
}