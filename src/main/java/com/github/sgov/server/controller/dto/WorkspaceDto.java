package com.github.sgov.server.controller.dto;

import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.OWLAnnotationProperty;
import cz.cvut.kbss.jopa.vocabulary.DC;
import cz.cvut.kbss.jsonld.annotation.JsonLdAttributeOrder;
import java.net.URI;
import lombok.Data;

@Data
@JsonLdAttributeOrder({"uri", "label"})
public class WorkspaceDto {

    @Id
    private URI uri;

    @OWLAnnotationProperty(iri = DC.Terms.TITLE)
    private String label;

    public WorkspaceDto(URI uri, String label) {
        this.uri = uri;
        this.label = label;
    }
}
