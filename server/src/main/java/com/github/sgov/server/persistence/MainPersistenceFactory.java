package com.github.sgov.server.persistence;

import static cz.cvut.kbss.jopa.model.JOPAPersistenceProperties.DATA_SOURCE_CLASS;
import static cz.cvut.kbss.jopa.model.JOPAPersistenceProperties.LANG;
import static cz.cvut.kbss.jopa.model.JOPAPersistenceProperties.ONTOLOGY_PHYSICAL_URI_KEY;
import static cz.cvut.kbss.jopa.model.JOPAPersistenceProperties.SCAN_PACKAGE;
import static cz.cvut.kbss.jopa.model.PersistenceProperties.JPA_PERSISTENCE_PROVIDER;

import com.github.sgov.server.config.conf.PersistenceConf;
import com.github.sgov.server.config.conf.RepositoryConf;
import cz.cvut.kbss.jopa.Persistence;
import cz.cvut.kbss.jopa.model.EntityManagerFactory;
import cz.cvut.kbss.jopa.model.JOPAPersistenceProvider;
import cz.cvut.kbss.ontodriver.config.OntoDriverProperties;
import cz.cvut.kbss.ontodriver.sesame.config.SesameOntoDriverProperties;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Sets up persistence and provides {@link EntityManagerFactory} as Spring bean.
 */
@Configuration
public class MainPersistenceFactory {

  private final RepositoryConf repositoryConf;
  private final PersistenceConf persistenceConf;

  private EntityManagerFactory emf;

  @Autowired
  public MainPersistenceFactory(RepositoryConf repositoryConf,
                                PersistenceConf persistenceConf) {
    this.repositoryConf = repositoryConf;
    this.persistenceConf = persistenceConf;
  }

  /**
   * Default persistence unit configuration parameters.
   *
   * <p>These include: package scan for entities, provider specification
   *
   * @return Map with defaults
   */
  public static Map<String, String> defaultParams() {
    final Map<String, String> map = new HashMap<>();
    map.put(SCAN_PACKAGE, "com.github.sgov.server.model");
    map.put(JPA_PERSISTENCE_PROVIDER, JOPAPersistenceProvider.class.getName());
    return map;
  }

  @Bean
  @Primary
  public EntityManagerFactory getEntityManagerFactory() {
    return emf;
  }

  @PostConstruct
  private void init() {
    // Allow Apache HTTP client used by RDF4J to use a larger connection pool
    // Temporary, should be configurable via JOPA
    System.setProperty("http.maxConnections", "20");
    final Map<String, String> properties = defaultParams();
    properties.put(ONTOLOGY_PHYSICAL_URI_KEY, repositoryConf.getUrl());
    properties.put(DATA_SOURCE_CLASS, persistenceConf.getDriver());
    properties.put(LANG, persistenceConf.getLanguage());
    if (repositoryConf.getUsername() != null) {
      properties.put(OntoDriverProperties.DATA_SOURCE_USERNAME, repositoryConf.getUsername());
      properties.put(OntoDriverProperties.DATA_SOURCE_PASSWORD, repositoryConf.getPassword());
    }
    // OPTIMIZATION: Always use statement retrieval with unbound property. Should spare
    // repository queries
    properties.put(SesameOntoDriverProperties.SESAME_LOAD_ALL_THRESHOLD, "1");
    this.emf = Persistence.createEntityManagerFactory("termitPU", properties);
  }

  @PreDestroy
  private void close() {
    if (emf.isOpen()) {
      emf.close();
    }
  }
}
