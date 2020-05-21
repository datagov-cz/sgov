package com.github.sgov.server.model;

import com.github.sgov.server.model.util.HasIdentifier;
import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.MappedSuperclass;

import java.net.URI;

/**
 * Represents basic info about an asset managed by the application.
 */
@MappedSuperclass
public abstract class Asset extends HasProvenanceData implements HasIdentifier {

    @Id(generated = true)
    private URI uri;

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public abstract String getLabel();

    public abstract void setLabel(String label);
}
