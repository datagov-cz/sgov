package com.github.sgov.server.model;

import com.github.sgov.server.util.Vocabulary;
import org.junit.jupiter.api.Test;

class UserTest {

    private User sut = new User();

    @Test
    void removeTypeHandlesNullTypesAttribute() {
        sut.removeType(Vocabulary.s_c_administrator);
    }
}