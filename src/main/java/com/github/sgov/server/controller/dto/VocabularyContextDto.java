package com.github.sgov.server.controller.dto;

import com.github.sgov.server.util.Vocabulary;
import cz.cvut.kbss.jopa.model.annotations.OWLAnnotationProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import cz.cvut.kbss.jopa.vocabulary.DC;
import java.net.URI;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@OWLClass(iri = Vocabulary.s_c_slovnikovy_kontext)
public class VocabularyContextDto {

    @OWLAnnotationProperty(iri = DC.Terms.TITLE)
    private String label;

    @OWLAnnotationProperty(iri = DC.Terms.DESCRIPTION)
    private String description;

    @OWLObjectProperty(iri = Vocabulary.s_p_vychazi_z_verze)
    private URI basedOnVersion;
}
