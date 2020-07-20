package com.github.sgov.server.model;

import com.github.sgov.server.exception.SGoVException;
import com.github.sgov.server.exception.ValidationException;
import com.github.sgov.server.provenance.ProvenanceManager;
import com.github.sgov.server.util.Vocabulary;
import cz.cvut.kbss.jopa.model.annotations.CascadeType;
import cz.cvut.kbss.jopa.model.annotations.EntityListeners;
import cz.cvut.kbss.jopa.model.annotations.FetchType;
import cz.cvut.kbss.jopa.model.annotations.OWLAnnotationProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import cz.cvut.kbss.jopa.model.annotations.ParticipationConstraints;
import cz.cvut.kbss.jopa.vocabulary.DC;
import cz.cvut.kbss.jsonld.annotation.JsonLdAttributeOrder;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.constraints.NotBlank;

@OWLClass(iri = Vocabulary.s_c_metadatovy_kontext)
@JsonLdAttributeOrder({"uri", "label", "author", "lastEditor"})
@EntityListeners(ProvenanceManager.class)
public class Workspace extends Asset implements Context {

    @NotBlank
    @ParticipationConstraints(nonEmpty = true)
    @OWLAnnotationProperty(iri = DC.Terms.TITLE)
    private String label;

    @OWLObjectProperty(iri = Vocabulary.s_p_odkazuje_na_kontext,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
            fetch = FetchType.EAGER)
    private Set<VocabularyContext> vocabularyContexts;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }


    /**
     * Returns all vocabulary contexts of this workspace.
     */
    public Set<VocabularyContext> getVocabularyContexts() {
        if (vocabularyContexts == null) {
            this.vocabularyContexts = new HashSet<>();
        }
        return vocabularyContexts;
    }

    /**
     * Add new vocabulary context to this workspace. Each vocabulary can be added only once.
     * @param context Vocabulary context to be added.
     */
    public void addRefersToVocabularyContexts(VocabularyContext context) {
        if (vocabularyContexts == null) {
            this.vocabularyContexts = new HashSet<>();
        }
        Optional<VocabularyContext> duplicateContext = vocabularyContexts.stream()
                .filter(vc -> vc.getBasedOnVocabularyVersion()
                        .equals(context.getBasedOnVocabularyVersion())
                )
                .findFirst();

        if (duplicateContext.isPresent()) {
            throw new ValidationException(String.format(
                    "Unable to add vocabulary %s to workspace %s. "
                            + "Vocabulary is already present in the workspace within context %s.",
                    duplicateContext.get().getBasedOnVocabularyVersion(),
                    this.getUri(),
                    duplicateContext.get().getUri()));
        }

        vocabularyContexts.add(context);
    }

    /**
     * Returns field vocabularyContexts.
     */
    public static Field getVocabularyContextsField() {
        try {
            return Workspace.class.getDeclaredField("vocabularyContexts");
        } catch (NoSuchFieldException e) {
            throw new SGoVException(
                "Fatal error! Unable to retrieve \"vocabularyContexts\" field.", e
            );
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Workspace)) {
            return false;
        }
        Workspace that = (Workspace) o;
        return Objects.equals(getUri(), that.getUri());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUri());
    }

    @Override
    public String toString() {
        return "Workspace{"
                + getLabel()
                + " <" + getUri() + '>'
                + (getVocabularyContexts().isEmpty() ? "" :
                ", vocabularies = [" + getVocabularyContexts().stream()
                        .map(p -> "<" + p.getBasedOnVocabularyVersion() + ">")
                        .collect(Collectors.joining(", ")) + "]")
                + '}';
    }

}