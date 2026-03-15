package com.github.sgov.server.model;

import com.github.sgov.server.util.Vocabulary;
import cz.cvut.kbss.jopa.model.annotations.*;
import cz.cvut.kbss.jsonld.annotation.JsonLdAttributeOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@OWLClass(iri = Vocabulary.s_c_slovnikovy_kontext)
@JsonLdAttributeOrder({"uri", "label", "basedOnVersion", "changeTrackingContext"})
public class VocabularyContext extends TrackableContext {

    @ParticipationConstraints(nonEmpty = true)
    @OWLObjectProperty(iri = Vocabulary.s_p_ma_kontext_sledovani_zmen,
        cascade = {CascadeType.PERSIST, CascadeType.REMOVE},
        fetch = FetchType.EAGER)
    private ChangeTrackingContext changeTrackingContext;

    @ParticipationConstraints
    @OWLObjectProperty(iri = Vocabulary.s_p_ma_prilohu,
        cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
        fetch = FetchType.EAGER)
    private Set<URI> attachments = new HashSet<>();

    @OWLObjectProperty(iri = Vocabulary.s_p_odkazuje_na_prilohovy_kontext,
        fetch = FetchType.EAGER)
    private Set<AttachmentContext> attachmentContexts;

    /**
     * Returns all attachment contexts of this vocabulary.
     */
    public Set<AttachmentContext> getAttachmentContexts() {
        if (attachmentContexts == null) {
            this.attachmentContexts = new HashSet<>();
        }
        return attachmentContexts;
    }

    /**
     * Add a new attachment context to this vocabulary.
     *
     * @param context attachment context to add.
     */
    public void addAttachmentContext(AttachmentContext context) {
        if (attachmentContexts == null) {
            this.attachmentContexts = new HashSet<>();
        }
        attachmentContexts.add(context);
    }
}
