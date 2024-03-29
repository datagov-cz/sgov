package com.github.sgov.server.model;

import com.github.sgov.server.util.Vocabulary;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import cz.cvut.kbss.jopa.model.annotations.ParticipationConstraints;
import java.net.URI;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@OWLClass(iri = Vocabulary.s_c_kontext_sledovani_zmen)
public class ChangeTrackingContext extends AbstractEntity implements Context {

    @ParticipationConstraints(nonEmpty = true)
    @OWLObjectProperty(iri = Vocabulary.s_p_meni_verzi)
    private URI changesVocabularyVersion;
}
