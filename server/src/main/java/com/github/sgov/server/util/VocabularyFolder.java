package com.github.sgov.server.util;

import static com.github.sgov.server.util.Vocabulary.SLOVNIK_GOV_CZ;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class VocabularyFolder {

    private final File folder;

    public VocabularyFolder(File folder) {
        this.folder = folder;
    }

    /**
     * Creates a new vocabulary folder for the given vocabulary.
     *
     * @param root Root folder for vocabularies (root of SSP repo)
     * @param vocabularyIri IRI of the vocabulary
     * @return vocabulary folder for the given vocabulary
     */
    public static VocabularyFolder ofVocabularyIri(final File root, final URI vocabularyIri) {
        final Pattern regex = Pattern.compile("^" + SLOVNIK_GOV_CZ + "/("
            + Arrays.stream(VocabularyType.values()).map(v -> v.fragment)
                .collect(Collectors.joining("|"))
            + ")(/(.*))?$");
        Matcher m = regex.matcher(vocabularyIri.toString());
        if (!m.matches()) {
            return null;
        } else {
            final VocabularyType type = Arrays
                .stream(VocabularyType.values())
                .filter(v -> v.fragment.equals(m.group(1)))
                .findAny().get();

            final String vocabularyId;
            if (m.group(3) != null) {
                vocabularyId = m.group(3).replace("/", "-");
            } else {
                vocabularyId = null;
            }

            final VocabularyFolder folder =
                new VocabularyFolder(
                    Paths.get(root.getAbsolutePath() + "/" + type.getVocabularyFolder(vocabularyId))
                        .toFile());
            return folder;
        }
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
     * @return array of files
     */
    public File[] toPruneAllExceptCompact() {
        return folder.listFiles((file, s) -> (s.contains("-model")
            || s.contains("-glosář")
            || s.contains("-slovník")
        ) && !s.endsWith("-compact.ttl"));
    }
}
