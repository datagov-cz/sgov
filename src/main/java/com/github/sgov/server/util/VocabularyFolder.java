package com.github.sgov.server.util;

import java.io.File;

public class VocabularyFolder {

    private final File folder;

    public VocabularyFolder(File folder) {
        this.folder = folder;
    }


    private String getVocabularyId() {
        final String p = folder.getAbsolutePath();
        return p.substring(p.lastIndexOf("/") + 1);
    }

    private File getFile(String type, String suffix) {
        return new File(folder,
            getVocabularyId() + "-" + type + (suffix.isEmpty() ? "" : "-" + suffix)
                + Constants.Turtle.FILE_EXTENSION);
    }

    public File getVocabularyFile(String suffix) {
        return getFile("slovník", suffix);
    }

    public File getGlossaryFile(String suffix) {
        return getFile("glosář", suffix);
    }

    public File getModelFile(String suffix) {
        return getFile("model", suffix);
    }

    public File getFolder() {
        return folder;
    }

    /**
     * Return all non-compact vocabulary files - to be deleted upon update.
     *
     * @return array of files
     */
    public File[] toPruneAllExceptCompact() {
        return folder.listFiles((file, s) -> (
            s.contains("-model")
                || s.contains("-glosář")
                || s.contains("-slovník")
        ) && !s.endsWith("-compact.ttl"));
    }
}
