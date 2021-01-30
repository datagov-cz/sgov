package com.github.sgov.server.model.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.sgov.server.environment.Generator;
import com.github.sgov.server.model.Workspace;
import cz.cvut.kbss.jopa.model.descriptors.Descriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DescriptorFactoryTest {

    private Workspace modelObject;

    @BeforeEach
    void setUp() {
        this.modelObject = Generator.generateWorkspace();
    }

    @Test
    void workspaceDescriptorCreatesSimpleUserDescriptor() {
        final Descriptor result = DescriptorFactory.workspaceDescriptor(modelObject);
        assertEquals(modelObject.getUri(), result.getContext());
    }
}
