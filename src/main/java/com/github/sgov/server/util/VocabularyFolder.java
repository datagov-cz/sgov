package com.github.sgov.server.util;

import java.io.File;
import java.nio.file.Paths;

public class VocabularyFolder {

    private final File folder;

    private VocabularyFolder(File folder) {
        this.folder = folder;
    }

    /**
     * Creates a new vocabulary folder for the given vocabulary.
     *
     * @param root     Root folder for vocabularies (root of SSP repo)
     * @param instance vocabulary
     * @return vocabulary folder for the given vocabulary
     */
    public static VocabularyFolder ofVocabularyIri(final File root,
                                                   final VocabularyInstance instance) {
        return new VocabularyFolder(
            Paths.get(root.getAbsolutePath() + "/" + instance.getFolder())
                .toFile());
    }

    public String getVocabularyId() {
        final String p = folder.getAbsolutePath();
        return p.substring(p.lastIndexOf("/") + 1);
    }

    private String getSuffixString(String suffix) {
        return (suffix.isEmpty() ? "" : "-" + suffix);
    }

    private File getFile(String type, String suffix) {
        return new File(folder,
            getVocabularyId() + "-" + type + getSuffixString(suffix)
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
