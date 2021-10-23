package com.github.sgov.server.model;

import com.github.sgov.server.model.util.HasIdentifier;
import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.MappedSuperclass;
import java.net.URI;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents basic info about an asset managed by the application.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@MappedSuperclass
public abstract class Asset extends HasProvenanceData implements HasIdentifier {

    @Id(generated = true)
    private URI uri;
}
