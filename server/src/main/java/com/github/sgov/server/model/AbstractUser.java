package com.github.sgov.server.model;

import com.github.sgov.server.model.util.HasIdentifier;
import com.github.sgov.server.model.util.HasTypes;
import com.github.sgov.server.util.Vocabulary;
import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.MappedSuperclass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jopa.model.annotations.ParticipationConstraints;
import cz.cvut.kbss.jopa.model.annotations.Transient;
import cz.cvut.kbss.jopa.model.annotations.Types;
import java.io.Serializable;
import java.net.URI;
import java.util.Set;
import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
@MappedSuperclass
abstract class AbstractUser implements HasIdentifier, HasTypes, Serializable {

    @Id
    protected URI uri;

    @NotBlank
    @ParticipationConstraints(nonEmpty = true)
    @OWLDataProperty(iri = Vocabulary.s_p_ma_krestni_jmeno)
    protected String firstName;

    @NotBlank
    @ParticipationConstraints(nonEmpty = true)
    @OWLDataProperty(iri = Vocabulary.s_p_ma_prijmeni)
    protected String lastName;

    @Transient
    protected String id;

    @Types
    protected Set<String> types;

    public String getId() {
        return uri != null
            ? uri.getPath().substring(uri.getPath().lastIndexOf('/') + 1)
            : null;
    }
}
