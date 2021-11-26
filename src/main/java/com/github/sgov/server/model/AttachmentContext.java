package com.github.sgov.server.model;

import com.github.sgov.server.util.Vocabulary;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jsonld.annotation.JsonLdAttributeOrder;

@OWLClass(iri = Vocabulary.s_c_prilohovy_kontext)
@JsonLdAttributeOrder({"uri", "label", "basedOnVersion"})
public class AttachmentContext extends TrackableContext {
}
