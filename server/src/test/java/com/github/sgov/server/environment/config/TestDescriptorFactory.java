package com.github.sgov.server.environment.config;

import com.github.sgov.server.model.util.DescriptorFactory;
import com.github.sgov.server.persistence.Persistence;
import com.github.sgov.server.persistence.PersistenceUtils;
import cz.cvut.kbss.jopa.model.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@TestConfiguration
@ComponentScan(basePackageClasses = {DescriptorFactory.class, Persistence.class})
public class TestDescriptorFactory {

    @Autowired
    private EntityManagerFactory emf;

    @Bean
    public DescriptorFactory descriptorFactory() {
        return new DescriptorFactory(new PersistenceUtils( emf ));
    }
}
