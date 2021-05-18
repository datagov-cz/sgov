package com.github.sgov.server.model.util;

import com.github.sgov.server.model.Workspace;
import com.github.sgov.server.persistence.PersistenceUtils;
import cz.cvut.kbss.jopa.model.descriptors.Descriptor;
import cz.cvut.kbss.jopa.model.descriptors.EntityDescriptor;
import cz.cvut.kbss.jopa.model.metamodel.FieldSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Objects;

/**
 * Provides descriptors for working with repository contexts.
 */
@Component
public final class DescriptorFactory {

    private final PersistenceUtils persistenceUtils;

    @Autowired
    public DescriptorFactory(PersistenceUtils persistenceUtils) {
        this.persistenceUtils = persistenceUtils;
    }

    /**
     * Gets field specification for the specified attribute from persistence unit metamodel.
     *
     * @param entityCls Entity class
     * @param attName   Name of attribute in the entity class
     * @return Metamodel field specification
     */
    public <T> FieldSpecification<? super T, ?> fieldSpec(Class<T> entityCls, String attName) {
        return persistenceUtils.getMetamodel().entity(entityCls).getFieldSpecification(attName);
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
    public Descriptor workspaceDescriptor(Workspace workspace) {
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
    public Descriptor workspaceDescriptor(URI workspaceUri) {
        Objects.requireNonNull(workspaceUri);
        EntityDescriptor descriptor = assetDescriptor(workspaceUri);
        descriptor.addAttributeDescriptor(fieldSpec(Workspace.class,"vocabularyContexts"), vocabularyDescriptor(workspaceUri));
        descriptor.addAttributeDescriptor(fieldSpec(Workspace.class,"author"), new EntityDescriptor((URI) null));
        descriptor.addAttributeDescriptor(fieldSpec(Workspace.class,"lastEditor"), new EntityDescriptor((URI) null));
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
