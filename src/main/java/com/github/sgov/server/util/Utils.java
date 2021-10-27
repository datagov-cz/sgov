package com.github.sgov.server.util;

import java.io.File;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.regex.Matcher;

public class Utils {

    /**
     * Creates a new vocabulary folder for the given vocabulary.
     *
     * @param root          Root folder for vocabularies (root of SSP repo)
     * @param vocabularyIri IRI of the vocabulary
     * @return vocabulary folder for the given vocabulary
     */
    public static VocabularyFolder getVocabularyFolder(final File root,
                                                       final String vocabularyIri) {
        return new VocabularyFolder(
            Paths.get(root.getAbsolutePath() + "/" + GitLayout.getVocabularyFolder(vocabularyIri))
                .toFile());
    }


    /**
     * Gets identifier of a new vocabulary, given its IRI.
     *
     * @param iri IRI of the vocabulary
     */
    public static String getVocabularyId(final String iri) {
        VocabularyType type = VocabularyType.getType(iri);

        if (type == null) {
            throw new IllegalArgumentException(
                MessageFormat.format("Unknown vocabulary type for IRI {0}", iri)
            );
        }

        switch (type) {
          case ZSGOV:
          case VSGOV:
              return type.getPrefix();

          case GSGOV:
          case LSGOV:
          case ASGOV:
          case DSGOV:
              final Matcher m = type.getRegex().matcher(iri);
              m.matches();
              final String id = m.group(2).replace("/", "-");
              final String vocabularyId = type.getPrefix()
                  + (type.equals(VocabularyType.LSGOV) ? "-sbírka-" : "-")
                  + id;
              return vocabularyId;
          default:
              throw new IllegalArgumentException(
                 MessageFormat.format("Unknown vocabulary type: {0}", type)
              );
        }
    }
}