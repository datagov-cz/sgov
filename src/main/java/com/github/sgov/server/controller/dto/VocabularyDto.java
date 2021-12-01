package com.github.sgov.server.controller.dto;

import com.github.sgov.server.model.ChangeTrackingContext;
import com.github.sgov.server.model.util.HasTypes;
import com.github.sgov.server.util.Vocabulary;
import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.OWLAnnotationProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import cz.cvut.kbss.jopa.model.annotations.Types;
import cz.cvut.kbss.jopa.vocabulary.DC;
import cz.cvut.kbss.jsonld.annotation.JsonLdAttributeOrder;
import java.net.URI;
import java.util.Set;
import lombok.Data;

@Data
@OWLClass(iri = Vocabulary.s_c_slovnikovy_kontext)
@JsonLdAttributeOrder({"uri", "label", "basedOnVersion", "changeTrackingContext"})
public class VocabularyDto implements HasTypes {

    @Id
    private URI uri;

    @Types
    private Set<String> types;

    @OWLAnnotationProperty(iri = DC.Terms.TITLE)
    private String label;

    @OWLObjectProperty(iri = Vocabulary.s_p_vychazi_z_verze)
    private URI basedOnVersion;

    @OWLObjectProperty(iri = Vocabulary.s_p_ma_kontext_sledovani_zmen)
    private ChangeTrackingContext changeTrackingContext;

    /**
     * Sets flag whether the vocabulary should be readonly.
     *
     * @param readonly True, if vocabulary context should be readonly.
     */
    public void setReadonly(boolean readonly) {
        if (readonly) {
            addType(Vocabulary.s_c_slovnikovy_kontext_pouze_pro_cteni);
        } else {
            removeType(Vocabulary.s_c_slovnikovy_kontext_pouze_pro_cteni);
        }
    }
}
