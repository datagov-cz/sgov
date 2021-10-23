package com.github.sgov.server.model.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.sgov.server.model.User;
import com.github.sgov.server.util.Vocabulary;
import org.junit.jupiter.api.Test;

class EntityToOwlClassMapperTest {

    @Test
    void getOwlClassForEntityReturnsClassIriForEntityClass() {
        final String result = EntityToOwlClassMapper.getOwlClassForEntity(User.class);
        assertEquals(Vocabulary.s_c_uzivatel, result);
    }

    @Test
    void getOwlClassForEntityThrowsIllegalArgumentForClassNotAnnotatedWithOwlClass() {
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> EntityToOwlClassMapper.getOwlClassForEntity(EntityToOwlClassMapper.class));
        assertEquals("Class " + EntityToOwlClassMapper.class + " is not an OWL entity.",
            ex.getMessage());
    }
}