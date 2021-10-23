package com.github.sgov.server.model;

import com.github.sgov.server.model.util.HasIdentifier;
import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.MappedSuperclass;
import java.io.Serializable;
import java.net.URI;
import lombok.Data;

@Data
@MappedSuperclass
public abstract class AbstractEntity implements HasIdentifier, Serializable {

    @Id(generated = true)
    private URI uri;
}
