package com.github.sgov.server.service.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.sgov.server.environment.Environment;
import com.github.sgov.server.environment.Generator;
import com.github.sgov.server.exception.ValidationException;
import com.github.sgov.server.model.UserAccount;
import com.github.sgov.server.model.util.DescriptorFactory;
import com.github.sgov.server.service.BaseServiceTestRunner;
import cz.cvut.kbss.jopa.model.EntityManager;
import java.net.URI;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

class UserRepositoryServiceTest extends BaseServiceTestRunner {

    @Autowired
    private EntityManager em;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepositoryService sut;

    @Test
    void existsByUsernameReturnsTrueForExistingUsername() {
        final UserAccount user = Generator.generateUserAccountWithPassword();
        transactional(() -> em.persist(user, DescriptorFactory.userManagementDescriptor(user)));

        assertTrue(sut.exists(user.getUsername()));
    }

    @Test
    void persistGeneratesIdentifierForUser() {
        final UserAccount user = Generator.generateUserAccount();
        user.setPassword("12345");
        user.setUri(null);
        sut.persist(user);
        assertNotNull(user.getUri());

        final UserAccount result = em.find(UserAccount.class, user.getUri());
        assertNotNull(result);
        assertEquals(user, result);
    }

    @Test
    void persistEncodesUserPassword() {
        final UserAccount user = Generator.generateUserAccount();
        final String plainPassword = "12345";
        user.setPassword(plainPassword);

        sut.persist(user);
        final UserAccount result = em.find(UserAccount.class, user.getUri());
        assertTrue(passwordEncoder.matches(plainPassword, result.getPassword()));
    }

    @Test
    void persistThrowsValidationExceptionWhenPasswordIsNull() {
        final UserAccount user = Generator.generateUserAccount();
        user.setPassword(null);
        final ValidationException ex =
            assertThrows(ValidationException.class, () -> sut.persist(user));
        assertThat(ex.getMessage(), containsString("password must not be blank"));
    }

    @Test
    void persistThrowsValidationExceptionWhenPasswordIsEmpty() {
        final UserAccount user = Generator.generateUserAccount();
        user.setPassword("");
        final ValidationException ex =
            assertThrows(ValidationException.class, () -> sut.persist(user));
        assertThat(ex.getMessage(), containsString("password must not be blank"));
    }

    @Test
    void updateEncodesPasswordWhenItWasChanged() {
        final UserAccount user = persistUser();
        Environment.setCurrentUser(user);
        final String plainPassword = "updatedPassword01";
        user.setPassword(plainPassword);

        sut.update(user);
        final UserAccount result = em.find(UserAccount.class, user.getUri());
        assertTrue(passwordEncoder.matches(plainPassword, result.getPassword()));
    }

    @Test
    void updateRetainsOriginalPasswordWhenItDoesNotChange() {
        final UserAccount user = Generator.generateUserAccountWithPassword();
        final String plainPassword = user.getPassword();
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        transactional(() -> em.persist(user, DescriptorFactory.userManagementDescriptor(user)));
        Environment.setCurrentUser(user);
        user.setPassword(null); // Simulate instance being loaded from repo
        final String newLastName = "newLastName";
        user.setLastName(newLastName);

        sut.update(user);
        final UserAccount result = em.find(UserAccount.class, user.getUri());
        assertTrue(passwordEncoder.matches(plainPassword, result.getPassword()));
        assertEquals(newLastName, result.getLastName());
    }

    private UserAccount persistUser() {
        final UserAccount user = Generator.generateUserAccountWithPassword();
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        transactional(() -> em.persist(user, DescriptorFactory.userManagementDescriptor(user)));
        return user;
    }

    @Test
    void updateThrowsValidationExceptionWhenUpdatedInstanceIsMissingValues() {
        final UserAccount user = persistUser();
        Environment.setCurrentUser(user);

        user.setUsername(null);
        user.setPassword(null); // Simulate instance being loaded from repo
        final ValidationException ex =
            assertThrows(ValidationException.class, () -> sut.update(user));
        assertThat(ex.getMessage(), containsString("username must not be blank"));
    }

    @Test
    void persistDoesNotGenerateUriIfItIsAlreadyPresent() {
        final UserAccount user = Generator.generateUserAccountWithPassword();
        final URI originalUri = user.getUri();
        sut.persist(user);

        final UserAccount result = em.find(UserAccount.class, originalUri);
        assertNotNull(result);
        assertEquals(originalUri, result.getUri());
    }
}
