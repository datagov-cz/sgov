package com.github.sgov.server.service;

import com.github.sgov.server.config.conf.FeatureConf;
import com.github.sgov.server.config.conf.PersistenceConf;
import com.github.sgov.server.config.conf.RepositoryConf;
import com.github.sgov.server.config.conf.UserConf;
import com.github.sgov.server.config.conf.components.ComponentsProperties;
import com.github.sgov.server.dao.AttachmentDao;
import com.github.sgov.server.dao.VocabularyDao;
import com.github.sgov.server.dao.WorkspaceDao;
import com.github.sgov.server.environment.TransactionalTestRunner;
import com.github.sgov.server.environment.config.TestDescriptorFactory;
import com.github.sgov.server.environment.config.TestPersistenceConfig;
import com.github.sgov.server.environment.config.TestServiceConfig;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@ExtendWith(SpringExtension.class)
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableTransactionManagement
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class,
    classes = {TestServiceConfig.class,
        VocabularyDao.class,
        WorkspaceDao.class,
        AttachmentDao.class,
        TestPersistenceConfig.class,
        PersistenceConf.class,
        RepositoryConf.class,
        FeatureConf.class,
        UserConf.class,
        ComponentsProperties.class,
        TestDescriptorFactory.class
    })
@ActiveProfiles("test")
public class BaseServiceTestRunner extends TransactionalTestRunner {
}
