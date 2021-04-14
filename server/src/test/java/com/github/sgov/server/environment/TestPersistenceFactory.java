package com.github.sgov.server.environment;

import static cz.cvut.kbss.jopa.model.JOPAPersistenceProperties.DATA_SOURCE_CLASS;
import static cz.cvut.kbss.jopa.model.JOPAPersistenceProperties.LANG;
import static cz.cvut.kbss.jopa.model.JOPAPersistenceProperties.ONTOLOGY_PHYSICAL_URI_KEY;


import com.github.sgov.server.config.conf.components.ComponentsProperties;
import com.github.sgov.server.config.conf.PersistenceConf;
import com.github.sgov.server.config.conf.RepositoryConf;
import com.github.sgov.server.environment.config.TestServiceConfig;
import com.github.sgov.server.persistence.MainPersistenceFactory;
import cz.cvut.kbss.jopa.Persistence;
import cz.cvut.kbss.jopa.model.EntityManagerFactory;
import cz.cvut.kbss.ontodriver.sesame.config.SesameOntoDriverProperties;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@TestConfiguration
@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class,
    classes = {TestServiceConfig.class, PersistenceConf.class, RepositoryConf.class,
        ComponentsProperties.class})
@ActiveProfiles("test")
public class TestPersistenceFactory {

    private final ComponentsProperties confComponents;

    private final PersistenceConf confPersistence;

    private EntityManagerFactory emf;

    @Autowired
    public TestPersistenceFactory(PersistenceConf confPersistence,
                                  ComponentsProperties confComponents) {
        this.confPersistence = confPersistence;
        this.confComponents = confComponents;
    }

    @Bean
    @Primary
    public EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }

    @PostConstruct
    private void init() {
        final Map<String, String> properties = MainPersistenceFactory.defaultParams();
        properties.put(ONTOLOGY_PHYSICAL_URI_KEY, confComponents.getComponents().getDbServerUrl());
        properties
            .put(SesameOntoDriverProperties.SESAME_USE_VOLATILE_STORAGE, Boolean.TRUE.toString());
        properties.put(SesameOntoDriverProperties.SESAME_USE_INFERENCE, Boolean.TRUE.toString());
        properties.put(DATA_SOURCE_CLASS, confPersistence.getDriver());
        properties.put(LANG, confPersistence.getLanguage());
        // OPTIMIZATION: Always use statement retrieval with unbound property. Should spare
        // repository queries
        properties.put(SesameOntoDriverProperties.SESAME_LOAD_ALL_THRESHOLD, "1");
        properties
            .put(SesameOntoDriverProperties.SESAME_REPOSITORY_CONFIG, "rdf4j-memory-spin-rdfs.ttl");
        this.emf = Persistence.createEntityManagerFactory("termitTestPU", properties);
    }

    @PreDestroy
    private void close() {
        if (emf.isOpen()) {
            emf.close();
        }
    }
}
