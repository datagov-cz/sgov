package com.github.sgov.server.environment.config;

import com.github.ledsoft.jopa.spring.transaction.DelegatingEntityManager;
import com.github.ledsoft.jopa.spring.transaction.JopaTransactionManager;
import com.github.sgov.server.environment.TestPersistenceFactory;
import com.github.sgov.server.persistence.Persistence;
import cz.cvut.kbss.jopa.model.EntityManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
@ComponentScan(basePackageClasses = {Persistence.class})
@Import({TestPersistenceFactory.class})
@EnableTransactionManagement
public class TestPersistenceConfig {

  @Bean
  public DelegatingEntityManager entityManager() {
    return new DelegatingEntityManager();
  }

  @Bean(name = "txManager")
  public PlatformTransactionManager transactionManager(EntityManagerFactory emf,
                                                       DelegatingEntityManager emProxy) {
    return new JopaTransactionManager(emf, emProxy);
  }
}
