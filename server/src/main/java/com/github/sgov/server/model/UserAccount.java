package com.github.sgov.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.sgov.server.exception.SGoVException;
import com.github.sgov.server.util.Vocabulary;
import cz.cvut.kbss.jopa.model.annotations.CascadeType;
import cz.cvut.kbss.jopa.model.annotations.FetchType;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import cz.cvut.kbss.jopa.model.annotations.ParticipationConstraints;
import java.lang.reflect.Field;
import java.util.HashSet;
import javax.validation.constraints.NotBlank;

@OWLClass(iri = Vocabulary.s_c_uzivatel)
@SuppressWarnings("checkstyle:MissingJavadocType")
public class UserAccount extends AbstractUser {

    @OWLObjectProperty(iri = Vocabulary.s_p_ma_pracovni_metadatovy_kontext,
            cascade = CascadeType.MERGE,
            fetch = FetchType.EAGER)
    private Workspace currentWorkspace;

    @NotBlank
    @ParticipationConstraints(nonEmpty = true)
    @OWLDataProperty(iri = Vocabulary.s_p_ma_heslo)
    private String password;

    /**
     * Returns the password field - to be used for descriptors.
     *
     * @return password field
     */
    public static Field getPasswordField() {
        try {
            return UserAccount.class.getDeclaredField("password");
        } catch (NoSuchFieldException e) {
            throw new SGoVException("Fatal error! Unable to retrieve \"password\" field.", e);
        }
    }

    /**
     * Returns field currentWorkspace.
     */
    public static Field getCurrentWorkspaceField() {
        try {
            return UserAccount.class.getDeclaredField("currentWorkspace");
        } catch (NoSuchFieldException e) {
            throw new SGoVException(
                "Fatal error! Unable to retrieve \"currentWorkspace\" field.", e
            );
        }
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Erases the password in this instance.
     *
     * <p>This should be used for security reasons when passing the instance throughout the
     * application and especially when it to be send from the REST API to the client.
     */
    public void erasePassword() {
        this.password = null;
    }

    /**
     * Checks whether the account represented by this instance is locked.
     *
     * @return Locked status
     */
    @JsonIgnore
    public boolean isLocked() {
        return types != null && types.contains(Vocabulary.s_c_uzamceny_uzivatel);
    }

    /**
     * Locks the account represented by this instance.
     */
    public void lock() {
        addType(Vocabulary.s_c_uzamceny_uzivatel);
    }

    /**
     * Unlocks the account represented by this instance.
     */
    public void unlock() {
        if (types == null) {
            return;
        }
        types.remove(Vocabulary.s_c_uzamceny_uzivatel);
    }

    /**
     * Enables the account represented by this instance.
     *
     * <p>Does nothing if the account is already enabled.
     */
    public void enable() {
        if (types == null) {
            return;
        }
        types.remove(Vocabulary.s_c_zablokovany_uzivatel);
    }

    /**
     * Checks whether the account represented by this instance is enabled.
     */
    @JsonIgnore
    public boolean isEnabled() {
        return types == null || !types.contains(Vocabulary.s_c_zablokovany_uzivatel);
    }

    /**
     * Disables the account represented by this instance.
     *
     * <p>Disabled account cannot be logged into and cannot be used to view/modify data.
     */
    public void disable() {
        addType(Vocabulary.s_c_zablokovany_uzivatel);
    }

    /**
     * Checks whether this account is administrator.
     *
     * @return {@code true} if this account is of administrator type
     */
    public boolean isAdmin() {
        return types != null && types.contains(Vocabulary.s_c_administrator);
    }

    /**
     * Transforms this security-related {@code UserAccount} instance to a domain-specific {@code
     * User} instance.
     *
     * @return new user instance based on this account
     */
    public User toUser() {
        final User user = new User();
        copyAttributes(user);
        return user;
    }

    public Workspace getCurrentWorkspace() {
        return currentWorkspace;
    }

    public void setCurrentWorkspace(Workspace currentWorkspace) {
        this.currentWorkspace = currentWorkspace;
    }

    private void copyAttributes(AbstractUser target) {
        target.setUri(uri);
        target.setFirstName(firstName);
        target.setLastName(lastName);
        target.setUsername(username);
        if (types != null) {
            target.setTypes(new HashSet<>(types));
        }
    }

    /**
     * Returns a copy of this user account.
     *
     * @return This instance's copy
     */
    public UserAccount copy() {
        final UserAccount clone = new UserAccount();
        copyAttributes(clone);
        clone.password = password;
        clone.setCurrentWorkspace(currentWorkspace);
        return clone;
    }
}
