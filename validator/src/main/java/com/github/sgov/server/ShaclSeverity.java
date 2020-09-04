package com.github.sgov.server;

import lombok.Getter;
import org.topbraid.shacl.vocabulary.SH;

@Getter
enum ShaclSeverity {
    VIOLATION(SH.Violation.getURI()),
    WARNING(SH.Warning.getURI()),
    INFO(SH.Info.getURI());

    private final String uri;

    ShaclSeverity(final String uri) {
        this.uri = uri;
    }
}
