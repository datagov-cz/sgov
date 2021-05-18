package com.github.sgov.server.model;

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
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
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
     *
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
}