package com.github.sgov.server.model;

import com.github.sgov.server.util.Vocabulary;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;

@OWLClass(iri = Vocabulary.s_c_uzivatel)
@SuppressWarnings("checkstyle:MissingJavadocType")
public class User extends AbstractUser {
}
