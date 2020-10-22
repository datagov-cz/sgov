package com.github.sgov.server.util;

import java.nio.file.Paths;
import java.util.regex.Matcher;

public class VocabularyInstance {

    private final String prefix;

    private final String iri;

    private VocabularyType type;

    private String vocabularyId;

    private String folder;

    /**
     * Creates a new Vocabulary instance given its iri.
     *
     * @param iri IRI of the vocabulary
     */
    public VocabularyInstance(String iri) {
        this.iri = iri;
        this.type = VocabularyType.getType(iri);

        if (type == null) {
            throw new IllegalArgumentException("Unknown vocabulary type for IRI " + iri);
        }

        switch (type) {
          case ZSGOV:
          case VSGOV:
              this.prefix = this.type.getPrefix() + "-pojem";
              this.folder = "content/" + this.type.getPrefix();
              this.vocabularyId = null;
              break;

          case GSGOV:
          case LSGOV:
          case ASGOV:
          case DSGOV:
              final Matcher m = this.type.getRegex().matcher(iri);
              m.matches();
              final String id = m.group(2).replace("/", "-");
              this.vocabularyId = new StringBuilder()
                  .append(this.type.getPrefix())
                  .append(type.equals(VocabularyType.LSGOV) ? "-sbírka-" : "-")
                  .append(id)
                  .toString();
              this.prefix = this.vocabularyId + "-pojem";
              this.folder = "content/" + Paths.get(type.getPrefix(), vocabularyId).toString();
              break;

          default:
              throw new AssertionError("Not covered vocabulary type: " + iri);
        }
    }

    public String getIri() {
        return iri;
    }

    public String getFolder() {
        return folder;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getConceptNamespace() {
        return iri + "/pojem/";
    }
}
