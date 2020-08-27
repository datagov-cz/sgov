package com.github.sgov.server.util;

import java.io.File;
import java.nio.file.Paths;

public class VocabularyFolder {

    private final File folder;

    public VocabularyFolder(File folder) {
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

    public File getVocabularyFile(String suffix) {
        return new File(folder,
            getVocabularyId() + "-slovník" + getSuffixString(suffix) + ".ttl");
    }

    public File getGlossaryFile(String suffix) {
        return new File(folder,
            getVocabularyId() + "-glosář" + getSuffixString(suffix) + ".ttl");
    }

    public File getModelFile(String suffix) {
        return new File(folder,
            getVocabularyId() + "-model" + getSuffixString(suffix) + ".ttl");
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
        return folder.listFiles((file, s) -> (s.contains("-model")
            || s.contains("-glosář")
            || s.contains("-slovník")
        ) && !s.endsWith("-compact.ttl"));
    }
}
