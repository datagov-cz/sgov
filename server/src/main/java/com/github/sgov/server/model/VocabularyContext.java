package com.github.sgov.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.sgov.server.exception.SGoVException;
import com.github.sgov.server.model.util.HasTypes;
import com.github.sgov.server.util.Vocabulary;
import cz.cvut.kbss.jopa.model.annotations.CascadeType;
import cz.cvut.kbss.jopa.model.annotations.FetchType;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import cz.cvut.kbss.jopa.model.annotations.ParticipationConstraints;
import cz.cvut.kbss.jopa.model.annotations.Types;
import cz.cvut.kbss.jsonld.annotation.JsonLdAttributeOrder;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.Objects;
import java.util.Set;

@OWLClass(iri = Vocabulary.s_c_slovnikovy_kontext)
@JsonLdAttributeOrder({"uri", "label", "comment", "author", "lastEditor"})
public class VocabularyContext extends AbstractEntity implements Context, HasTypes {

    @Types
    Set<String> types;

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

    /**
     * Returns field changeTrackingContext.
     */
    public static Field getChangeTrackingContextField() {
        try {
            return VocabularyContext.class.getDeclaredField("changeTrackingContext");
        } catch (NoSuchFieldException e) {
            throw new SGoVException(
                "Fatal error! Unable to retrieve \"changeTrackingContext\" field.", e
            );
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VocabularyContext)) {
            return false;
        }
        VocabularyContext that = (VocabularyContext) o;
        return Objects.equals(getUri(), that.getUri());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUri());
    }

    @Override
    public String toString() {
        return "VocabularyContext{"
                + " <" + getUri() + '>'
                + ", basedOnVocabularyVersion=" + getBasedOnVocabularyVersion()
                + '}';
    }

}
