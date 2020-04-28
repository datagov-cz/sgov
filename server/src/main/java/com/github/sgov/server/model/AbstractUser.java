package com.github.sgov.server.model;

import com.github.sgov.server.model.util.HasIdentifier;
import com.github.sgov.server.model.util.HasTypes;
import com.github.sgov.server.util.Vocabulary;
import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.MappedSuperclass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jopa.model.annotations.ParticipationConstraints;
import cz.cvut.kbss.jopa.model.annotations.Types;
import java.io.Serializable;
import java.net.URI;
import java.util.Objects;
import java.util.Set;
import javax.validation.constraints.NotBlank;

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

    @NotBlank
    @ParticipationConstraints(nonEmpty = true)
    @OWLDataProperty(iri = Vocabulary.s_p_ma_uzivatelske_jmeno)
    protected String username;

    @Types
    protected Set<String> types;

    @Override
    public URI getUri() {
        return uri;
    }

    @Override
    public void setUri(URI uri) {
        this.uri = uri;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public Set<String> getTypes() {
        return types;
    }

    @Override
    public void setTypes(Set<String> types) {
        this.types = types;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AbstractUser)) {
            return false;
        }
        final AbstractUser that = (AbstractUser) o;
        return Objects.equals(uri, that.uri) && Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri, username);
    }

    @Override
    public String toString() {
        return "User{"
            + firstName
            + " " + lastName
            + ", username='" + username + '\''
            + '}';
    }
}
