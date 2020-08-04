package com.github.sgov.server.model;

import cz.cvut.kbss.jopa.model.annotations.*;
import cz.cvut.kbss.jopa.vocabulary.DC;
import cz.cvut.kbss.jsonld.annotation.JsonLdAttributeOrder;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Objects;

@OWLClass(iri = com.github.sgov.server.util.Vocabulary.s_c_slovnik)
@JsonLdAttributeOrder({"uri", "label", "description"})
public class Vocabulary extends Asset implements Serializable {

    @NotBlank
    @ParticipationConstraints(nonEmpty = true)
    @OWLAnnotationProperty(iri = DC.Terms.TITLE)
    private String label;

    @OWLAnnotationProperty(iri = DC.Terms.DESCRIPTION)
    private String description;

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Vocabulary)) {
            return false;
        }
        Vocabulary that = (Vocabulary) o;
        return Objects.equals(getUri(), that.getUri());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUri());
    }

    @Override
    public String toString() {
        return "Vocabulary {"
            + getLabel()
            + " <" + getUri() + '>' +
            '}';
    }
}
