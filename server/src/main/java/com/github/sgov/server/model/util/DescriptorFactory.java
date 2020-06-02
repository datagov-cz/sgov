package com.github.sgov.server.model.util;

import com.github.sgov.server.model.HasProvenanceData;
import com.github.sgov.server.model.UserAccount;
import com.github.sgov.server.model.Workspace;
import com.github.sgov.server.util.Vocabulary;
import cz.cvut.kbss.jopa.model.descriptors.Descriptor;
import cz.cvut.kbss.jopa.model.descriptors.EntityDescriptor;
import java.net.URI;
import java.util.Objects;

/**
 * Provides descriptors for working with repository contexts.
 */
public final class DescriptorFactory {

    private DescriptorFactory() {
        throw new AssertionError();
    }

    /**
     * Creates a JOPA descriptor for the specified user account.
     *
     * <p>The descriptor specifies that the instance context will correspond to the
     * {@code vocabulary}'s IRI. It also initializes other required attribute descriptors.
     *
     * @param userAccount User Account for which the descriptor should be created
     * @return user account descriptor
     */
    public static Descriptor userManagementDescriptor(UserAccount userAccount) {
        Objects.requireNonNull(userAccount);
        final EntityDescriptor descriptor
            = new EntityDescriptor(URI.create(Vocabulary.s_c_uzivatel));
        descriptor
            .addAttributeDescriptor(UserAccount.getPasswordField(),
                new EntityDescriptor(null));
        return descriptor;
    }

    /**
     * Creates a JOPA descriptor for the specified workspace.
     *
     * <p>The descriptor specifies that the instance context will correspond to the
     * {@code workspace}'s IRI. It also initializes other required attribute descriptors.
     *
     * @param workspace Workspace for which the descriptor should be created
     * @return workspace descriptor
     */
    public static Descriptor workspaceDescriptor(Workspace workspace) {
        Objects.requireNonNull(workspace);
        return workspaceDescriptor(workspace.getUri());
    }

    /**
     * Creates a JOPA descriptor for a workspace with the specified identifier.
     *
     * <p>The descriptor specifies that the instance context will correspond to the given IRI.
     * It also initializes otherrequired attribute descriptors.
     *
     * <p>Note that default context is used for asset author.
     *
     * @param workspaceUri Workspace identifier for which the descriptor should be created
     * @return Workspace descriptor
     */
    public static Descriptor workspaceDescriptor(URI workspaceUri) {
        Objects.requireNonNull(workspaceUri);
        EntityDescriptor descriptor = new EntityDescriptor(workspaceUri);
        descriptor.addAttributeDescriptor(
            HasProvenanceData.getAuthorField(), new EntityDescriptor(null)
        );
        descriptor.addAttributeDescriptor(
            HasProvenanceData.getLastEditorField(), new EntityDescriptor(null)
        );
        return descriptor;
    }
}
