package com.github.sgov.server.model.util;

import com.github.sgov.server.model.UserAccount;
import cz.cvut.kbss.jopa.model.descriptors.Descriptor;
import cz.cvut.kbss.jopa.model.descriptors.EntityDescriptor;
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
        final EntityDescriptor descriptor = new EntityDescriptor(userAccount.getUri());
        descriptor
            .addAttributeDescriptor(UserAccount.getPasswordField(), new EntityDescriptor(null));
        return descriptor;
    }
}
