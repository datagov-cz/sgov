package com.github.sgov.server.model.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.sgov.server.config.conf.PersistenceConf;
import com.github.sgov.server.config.conf.RepositoryConf;
import com.github.sgov.server.config.conf.components.ComponentsConf;
import com.github.sgov.server.config.conf.components.ComponentsConverter;
import com.github.sgov.server.config.conf.components.ComponentsProperties;
import com.github.sgov.server.environment.Generator;
import com.github.sgov.server.environment.config.TestDescriptorFactory;
import com.github.sgov.server.environment.config.TestPersistenceConfig;
import com.github.sgov.server.model.Workspace;
import cz.cvut.kbss.jopa.model.descriptors.Descriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class, classes = {
        TestDescriptorFactory.class,
        TestPersistenceConfig.class,
        PersistenceConf.class,
        RepositoryConf.class,
        ComponentsProperties.class,
        ComponentsConverter.class,
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
class DescriptorFactoryTest {

    private Workspace modelObject;

    @Autowired
    private DescriptorFactory descriptorFactory;

    @BeforeEach
    void setUp() {
        this.modelObject = Generator.generateWorkspace();
    }

    @Test
    void workspaceDescriptorCreatesSimpleUserDescriptor() {
        final Descriptor result = descriptorFactory.workspaceDescriptor(modelObject);
        assertEquals(modelObject.getUri(), result.getSingleContext().get());
    }
}
