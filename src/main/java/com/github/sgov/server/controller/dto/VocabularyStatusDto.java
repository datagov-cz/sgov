package com.github.sgov.server.controller.dto;

import cz.cvut.kbss.jsonld.annotation.JsonLdAttributeOrder;
import lombok.Data;

@Data
@JsonLdAttributeOrder({"published", "edited"})
public class VocabularyStatusDto {
    private boolean published;
    private boolean edited;

    public VocabularyStatusDto(boolean vocabularyPublished, boolean vocabularyEdited) {
        this.published = vocabularyPublished;
        this.edited = vocabularyEdited;
    }
}
