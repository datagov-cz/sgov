package com.github.sgov.server.model;

import com.github.sgov.server.util.Vocabulary;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import lombok.EqualsAndHashCode;

@OWLClass(iri = Vocabulary.s_c_uzivatel)
@SuppressWarnings("checkstyle:MissingJavadocType")
@EqualsAndHashCode(callSuper = true)
public class User extends AbstractUser {
}
