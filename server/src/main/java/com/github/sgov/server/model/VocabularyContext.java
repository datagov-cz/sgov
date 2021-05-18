package com.github.sgov.server.model;

import com.github.sgov.server.model.util.HasTypes;
import com.github.sgov.server.util.Vocabulary;
import cz.cvut.kbss.jopa.model.annotations.CascadeType;
import cz.cvut.kbss.jopa.model.annotations.FetchType;
import cz.cvut.kbss.jopa.model.annotations.Inferred;
import cz.cvut.kbss.jopa.model.annotations.OWLAnnotationProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import cz.cvut.kbss.jopa.model.annotations.ParticipationConstraints;
import cz.cvut.kbss.jopa.model.annotations.Types;
import cz.cvut.kbss.jopa.vocabulary.DC;
import cz.cvut.kbss.jsonld.annotation.JsonLdAttributeOrder;
import java.net.URI;
import java.util.Set;
import lombok.Data;

@Data
@OWLClass(iri = Vocabulary.s_c_slovnikovy_kontext)
@JsonLdAttributeOrder({"uri", "label", "basedOnVocabularyVersion", "changeTrackingContext"})
public class VocabularyContext extends AbstractEntity implements Context, HasTypes {

    @Types
    Set<String> types;

    @Inferred
    @OWLAnnotationProperty(iri = DC.Terms.TITLE)
    private String label;

    @ParticipationConstraints(nonEmpty = true)
    @OWLObjectProperty(iri = Vocabulary.s_p_vychazi_z_verze)
    private URI basedOnVocabularyVersion;

    @ParticipationConstraints(nonEmpty = true)
    @OWLObjectProperty(iri = Vocabulary.s_p_ma_kontext_sledovani_zmen,
        cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
        fetch = FetchType.EAGER)
    private ChangeTrackingContext changeTrackingContext;
}
