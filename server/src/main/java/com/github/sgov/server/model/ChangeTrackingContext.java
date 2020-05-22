package com.github.sgov.server.model;

import com.github.sgov.server.util.Vocabulary;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import cz.cvut.kbss.jopa.model.annotations.ParticipationConstraints;
import java.net.URI;
import java.util.Objects;

@OWLClass(iri = Vocabulary.s_c_kontext_sledovani_zmen)
public class ChangeTrackingContext extends AbstractEntity implements Context {

    @ParticipationConstraints(nonEmpty = true)
    @OWLObjectProperty(iri = Vocabulary.s_p_meni_verzi)
    private URI changesVocabularyVersion;

    public URI getChangesVocabularyVersion() {
        return changesVocabularyVersion;
    }

    public void setChangesVocabularyVersion(URI changesVocabularyVersion) {
        this.changesVocabularyVersion = changesVocabularyVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ChangeTrackingContext)) {
            return false;
        }
        ChangeTrackingContext that = (ChangeTrackingContext) o;
        return Objects.equals(getUri(), that.getUri());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUri());
    }

    @Override
    public String toString() {
        return "ChangeTrackingContext{"
                + " <" + getUri() + '>'
                + ", changingVocabulary=" + getChangesVocabularyVersion()
                + '}';
    }
}
