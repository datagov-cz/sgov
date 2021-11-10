package com.github.sgov.server.util;

import java.io.File;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.regex.Matcher;

public class Utils {

    public static final String CONTENT_ROOT = "obsah";

    /**
     * Creates a new vocabulary folder for the given vocabulary.
     *
     * @param root          Root folder for vocabularies
     * @param vocabularyIri IRI of the vocabulary
     * @return folder for the given vocabulary
     */
    public static VocabularyFolder getVocabularyFolder(final File root,
                                                       final String vocabularyIri) {
        return new VocabularyFolder(
            Paths.get(root.getAbsolutePath() + "/"
                    + (CONTENT_ROOT + "/slovníky/" + getVocabularyId(vocabularyIri)))
                .toFile());
    }

    /**
     * Creates a new asset folder for the given asset.
     *
     * @param root          Root folder for assets
     * @param assetIri      IRI of the asset
     * @return folder for the given asset
     */
    public static AssetFolder getAssetFolder(final File root,
                                             final String assetIri) {
        return new AssetFolder(
            Paths.get(root.getAbsolutePath() + "/"
                   + (CONTENT_ROOT + "/přílohy/" + getAssetId(assetIri)))
                .toFile());
    }

    /**
     * Gets id of the asset with the given IRI.
     *
     * @param iri of the asset
     * @return identifier of the given asset
     */
    public static String getAssetId(final String iri) {
        return iri.substring(iri.lastIndexOf("/") + 1);
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