package com.github.sgov.server.util;

import java.nio.file.Paths;
import java.util.regex.Matcher;
import lombok.Getter;

@Getter
public class VocabularyInstance {


    private final String iri;

    private String conceptPrefix;

    private String folder;

    /**
     * Creates a new Vocabulary instance given its iri.
     *
     * @param iri IRI of the vocabulary
     */
    public VocabularyInstance(String iri) {
        this.iri = iri;
        parseIri();
    }

    private void parseIri() {
        VocabularyType type = VocabularyType.getType(iri);

        if (type == null) {
            throw new IllegalArgumentException("Unknown vocabulary type for IRI " + iri);
        }

        switch (type) {
          case ZSGOV:
          case VSGOV:
              this.conceptPrefix = type.getPrefix() + "-pojem";
              this.folder = "content/" + type.getPrefix();
              break;

          case GSGOV:
          case LSGOV:
          case ASGOV:
          case DSGOV:
              final Matcher m = type.getRegex().matcher(iri);
              m.matches();
              final String id = m.group(2).replace("/", "-");
              String vocabularyId = type.getPrefix()
                  + (type.equals(VocabularyType.LSGOV) ? "-sbírka-" : "-")
                  + id;
              this.conceptPrefix = vocabularyId + "-pojem";
              this.folder = "content/" + Paths.get(type.getPrefix(), vocabularyId);
              break;

          default:
              throw new AssertionError("Not covered vocabulary type: " + iri);
        }
    }

    public String getConceptNamespace() {
        return iri + "/pojem/";
    }
}
