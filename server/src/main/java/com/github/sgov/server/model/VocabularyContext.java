package com.github.sgov.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import lombok.Data;

import java.net.URI;
import java.util.Set;

@Data
@OWLClass(iri = Vocabulary.s_c_slovnikovy_kontext)
@JsonLdAttributeOrder({"uri", "label", "comment", "author", "lastEditor"})
public class VocabularyContext extends AbstractEntity implements Context, HasTypes {

    @Types
    Set<String> types;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

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

    public URI getBasedOnVocabularyVersion() {
        return basedOnVocabularyVersion;
    }

    public void setBasedOnVocabularyVersion(URI basedOnVocabularyVersion) {
        this.basedOnVocabularyVersion = basedOnVocabularyVersion;
    }

    public ChangeTrackingContext getChangeTrackingContext() {
        return changeTrackingContext;
    }

    public void setChangeTrackingContext(ChangeTrackingContext changeTrackingContext) {
        this.changeTrackingContext = changeTrackingContext;
    }

    /**
     * Checks whether the vocabulary context represented by this instance is readonly.
     *
     * @return Locked status
     */
    @JsonIgnore
    public boolean isReadonly() {
        return types != null && types.contains(Vocabulary.s_c_slovnikovy_kontext_pouze_pro_cteni);
    }

    /**
     * Sets flag whether vocabulary context is readonly.
     * @param readonly True, if vocabulary context should be readonly.
     */
    public void setReadonly(boolean readonly) {
        if (readonly) {
            addType(Vocabulary.s_c_slovnikovy_kontext_pouze_pro_cteni);
        } else {
            removeType(Vocabulary.s_c_slovnikovy_kontext_pouze_pro_cteni);
        }
    }

    @Override
    public Set<String> getTypes() {
        return types;
    }

    @Override
    public void setTypes(Set<String> types) {
        this.types = types;
    }
}
