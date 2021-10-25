package com.github.sgov.server.persistence;

import cz.cvut.kbss.jopa.model.EntityManagerFactory;
import cz.cvut.kbss.jopa.model.metamodel.Metamodel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PersistenceUtils {

    private final EntityManagerFactory emf;

    @Autowired
    public PersistenceUtils(EntityManagerFactory emf) {
        this.emf = emf;
    }

    /**
     * Gets JOPA metamodel.
     *
     * @return Metamodel of the persistence unit
     */
    public Metamodel getMetamodel() {
        return emf.getMetamodel();
    }
}

