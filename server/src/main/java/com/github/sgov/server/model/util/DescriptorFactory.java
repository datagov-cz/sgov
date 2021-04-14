package com.github.sgov.server.model.util;

import com.github.sgov.server.model.HasProvenanceData;
import com.github.sgov.server.model.Workspace;
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
     * It also initializes other required attribute descriptors.
     *
     * <p>Note that default context is used for asset author.
     *
     * @param workspaceUri Workspace identifier for which the descriptor should be created
     * @return Workspace descriptor
     */
    public static Descriptor workspaceDescriptor(URI workspaceUri) {
        Objects.requireNonNull(workspaceUri);
        EntityDescriptor descriptor = assetDescriptor(workspaceUri);
        descriptor.addAttributeDescriptor(Workspace.getVocabularyContextsField(),
            vocabularyDescriptor(workspaceUri));
        descriptor.addAttributeDescriptor(
            HasProvenanceData.getAuthorField(), new EntityDescriptor(null)
        );
        descriptor.addAttributeDescriptor(
            HasProvenanceData.getLastEditorField(), new EntityDescriptor(null)
        );
        return descriptor;
    }

    private static EntityDescriptor assetDescriptor(URI iri) {
        if (iri == null) {
            return new EntityDescriptor();
        }
        return new EntityDescriptor(iri);
    }

    /**
     * Creates a JOPA descriptor for a vocabulary with the specified identifier.
     *
     * <p>The descriptor specifies that the instance context will correspond to the given IRI.
     * It also initializes other required attribute descriptors.
     *
     * @param vocabularyContextUri Workspace identifier for which the descriptor should be created
     * @return Vocabulary descriptor
     */
    public static Descriptor vocabularyDescriptor(URI vocabularyContextUri) {
        Objects.requireNonNull(vocabularyContextUri);
        return new EntityDescriptor(vocabularyContextUri);
    }
}
