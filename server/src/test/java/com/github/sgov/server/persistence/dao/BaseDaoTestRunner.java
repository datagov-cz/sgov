package com.github.sgov.server.persistence.dao;

import com.github.sgov.server.config.conf.components.ComponentsConverter;
import com.github.sgov.server.config.conf.components.ComponentsProperties;
import com.github.sgov.server.config.conf.PersistenceConf;
import com.github.sgov.server.config.conf.RepositoryConf;
import com.github.sgov.server.environment.TransactionalTestRunner;
import com.github.sgov.server.environment.config.TestPersistenceConfig;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class, classes = {
    TestPersistenceConfig.class,
    PersistenceConf.class,
    RepositoryConf.class,
    ComponentsProperties.class,
    ComponentsConverter.class,
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public abstract class BaseDaoTestRunner extends TransactionalTestRunner {
}
