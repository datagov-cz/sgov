package com.github.sgov.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.sgov.server.util.Vocabulary;
import cz.cvut.kbss.jopa.model.annotations.FetchType;
import cz.cvut.kbss.jopa.model.annotations.MappedSuperclass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import cz.cvut.kbss.jopa.model.annotations.ParticipationConstraints;
import lombok.Data;

import java.util.Date;

@Data
@MappedSuperclass
public abstract class HasProvenanceData {

    @ParticipationConstraints(nonEmpty = true)
    @OWLObjectProperty(iri = Vocabulary.s_p_ma_autora, fetch = FetchType.EAGER)
    private User author;

    @ParticipationConstraints(nonEmpty = true)
    @OWLDataProperty(iri = Vocabulary.s_p_ma_datum_a_cas_vytvoreni)
    private Date created;

    @OWLObjectProperty(iri = Vocabulary.s_p_ma_posledniho_editora, fetch = FetchType.EAGER)
    private User lastEditor;

    @OWLDataProperty(iri = Vocabulary.s_p_ma_datum_a_cas_posledni_modifikace)
    private Date lastModified;

    /**
     * Gets the datetime of this asset's last modification.
     *
     * <p>If {@link #getLastModified()} is set, its value is returned.
     * Otherwise, {@link #getCreated()} is returned.
     *
     * @return Datetime of the last modification, if present, or creation of this asset
     */
    @JsonIgnore
    public Date getLastModifiedOrCreated() {
        return lastModified != null ? lastModified : created;
    }
}