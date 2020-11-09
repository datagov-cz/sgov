package com.github.sgov.server.persistence.dao;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.sgov.server.dao.BaseDao;
import com.github.sgov.server.environment.Generator;
import com.github.sgov.server.exception.PersistenceException;
import com.github.sgov.server.model.User;
import cz.cvut.kbss.jopa.exceptions.OWLPersistenceException;
import cz.cvut.kbss.jopa.model.EntityManager;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@Tag("dao")
class BaseDaoTest extends BaseDaoTestRunner {

    @Autowired
    private EntityManager em;

    private BaseDao<User> sut;

    @BeforeEach
    void setUp() {
        this.sut = new BaseDaoImpl(em);
    }

    @Test
    void findAllRetrievesAllExistingInstances() {
        final List<User> users =
            IntStream.range(0, 5).mapToObj(i -> {
                final User u = Generator.generateUser();
                u.setUri(Generator.generateUri());
                return u;
            }).collect(Collectors.toList());
        transactional(() -> sut.persist(users));
        final List<User> result = sut.findAll();
        assertEquals(users.size(), result.size());
        assertTrue(users.containsAll(result));
    }

    @Test
    void existsReturnsTrueForExistingEntity() {
        final User user = Generator.generateUser();
        user.setUri(Generator.generateUri());
        transactional(() -> sut.persist(user));
        assertTrue(sut.exists(user.getUri()));
    }

    @Test
    void existsReturnsFalseForNonexistentEntity() {
        assertFalse(sut.exists(Generator.generateUri()));
    }

    @Test
    void findReturnsNonEmptyOptionalForExistingEntity() {
        final User user = Generator.generateUserWithId();
        transactional(() -> sut.persist(user));
        final Optional<User> result = sut.find(user.getUri());
        assertTrue(result.isPresent());
        assertEquals(user, result.get());
    }

    @Test
    void findReturnsEmptyOptionalForUnknownIdentifier() {
        final Optional<User> result = sut.find(Generator.generateUri());
        assertNotNull(result);
        assertFalse(result.isPresent());
    }

    @Test
    void updateReturnsManagedInstance() {
        final User user = Generator.generateUserWithId();
        transactional(() -> sut.persist(user));
        final String lastNameUpdate = "updatedLastName";
        user.setLastName(lastNameUpdate);
        transactional(() -> {
            final User updated = sut.update(user);
            assertTrue(em.contains(updated));
            assertEquals(lastNameUpdate, updated.getLastName());
        });
        assertEquals(lastNameUpdate, em.find(User.class, user.getUri()).getLastName());
    }

    @Test
    void removeRemovesEntity() {
        final User user = Generator.generateUserWithId();
        transactional(() -> sut.persist(user));
        transactional(() -> sut.remove(user));
        assertFalse(sut.exists(user.getUri()));
    }

    @Test
    void removeHandlesNonexistentEntity() {
        final User user = Generator.generateUserWithId();
        transactional(() -> sut.remove(user));
        assertFalse(sut.exists(user.getUri()));
    }

    @Test
    void removeByIdRemovesEntityWithSpecifiedIdentifier() {
        final User user = Generator.generateUserWithId();
        transactional(() -> sut.persist(user));
        transactional(() -> sut.remove(user.getUri()));
        assertFalse(sut.find(user.getUri()).isPresent());
    }

    @Test
    void exceptionDuringPersistIsWrappedInPersistenceException() {
        final PersistenceException e = assertThrows(PersistenceException.class, () -> {
            final User user = Generator.generateUser();
            transactional(() -> sut.persist(user));
        });
        assertThat(e.getCause(), is(instanceOf(OWLPersistenceException.class)));
    }

    @Test
    void exceptionDuringCollectionPersistIsWrappedInPersistenceException() {
        final List<User> users = Collections.singletonList(Generator.generateUserWithId());
        transactional(() -> sut.persist(users));

        final PersistenceException e = assertThrows(PersistenceException.class,
            () -> transactional(() -> sut.persist(users)));
        assertThat(e.getCause(), is(instanceOf(OWLPersistenceException.class)));
    }

    @Test
    void exceptionDuringUpdateIsWrappedInPersistenceException() {
        final User user = Generator.generateUserWithId();
        transactional(() -> sut.persist(user));
        user.setUri(null);
        final PersistenceException e = assertThrows(PersistenceException.class,
            () -> transactional(() -> sut.update(user)));
        assertThat(e.getCause(), is(instanceOf(OWLPersistenceException.class)));
    }

    @Test
    void getReferenceRetrievesReferenceToMatchingInstance() {
        final User user = Generator.generateUserWithId();
        transactional(() -> sut.persist(user));
        final Optional<User> result = sut.getReference(user.getUri());
        assertTrue(result.isPresent());
        assertEquals(user.getUri(), result.get().getUri());
    }

    @Test
    void getReferenceReturnsEmptyOptionalWhenNoMatchingInstanceExists() {
        final Optional<User> result = sut.getReference(Generator.generateUri());
        assertNotNull(result);
        assertFalse(result.isPresent());
    }

    private static class BaseDaoImpl extends BaseDao<User> {

        BaseDaoImpl(EntityManager em) {
            super(User.class, em);
        }
    }
}