package com.github.sgov.server.util;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.UUID;
import java.util.regex.Matcher;

public class Utils {

    public static final String CONTENT_ROOT = "content";

    /**
     * Creates a new vocabulary folder for the given vocabulary.
     *
     * @param root Root folder for vocabularies
     * @param iri  IRI of the vocabulary
     * @return folder for the given vocabulary
     */
    public static VocabularyFolder getVocabularyFolder(final File root,
                                                       final String iri) {
        return new VocabularyFolder(
            Paths.get(root.getAbsolutePath() + "/"
                    + (CONTENT_ROOT + "/vocabularies/" + getVocabularyId(iri)))
                .toFile());
    }

    /**
     * Creates a new attachment folder for the given attachment.
     *
     * @param root Root folder for attachments
     * @param iri  IRI of the attachment
     * @return folder for the given attachment
     */
    public static AttachmentFolder getAttachmentFolder(final File root,
                                                       final String iri) {
        return new AttachmentFolder(
            Paths.get(root.getAbsolutePath() + "/"
                    + (CONTENT_ROOT + "/attachments/" + getAttachmentId(iri)))
                .toFile());
    }

    /**
     * Returns a relative path to folder from a given root folder.
     *
     * @param folder given folder
     * @param root   root folder
     * @return relative path to the given vocabulary
     */
    public static Path getRelativePathToFolder(final File root,
                                               final Folder folder) {
        Path folderPath = Paths.get(folder.getFolder().getAbsolutePath());
        Path rootPath = Paths.get(root.getAbsolutePath());
        return rootPath.relativize(folderPath);
    }

    /**
     * Gets id of the attachment with the given IRI.
     *
     * @param iri of the attachment
     * @return identifier of the given attachment
     */
    public static String getAttachmentId(final String iri) {
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

    /**
     * Creates a version (context) URI from given URI.
     *
     * @param uri base URI
     * @return version (context) URI
     */
    public static URI createVersion(URI uri) {
        return URI.create(uri.toString() + Vocabulary.version_separator + UUID.randomUUID());
    }
}