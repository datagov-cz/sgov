package com.github.sgov.server.service.security;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.sgov.server.dao.UserAccountDao;
import com.github.sgov.server.environment.Environment;
import com.github.sgov.server.environment.Generator;
import com.github.sgov.server.exception.ValidationException;
import com.github.sgov.server.model.UserAccount;
import com.github.sgov.server.security.model.SGoVUserDetails;
import com.github.sgov.server.service.BaseServiceTestRunner;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

class SecurityUtilsTest extends BaseServiceTestRunner {

    @Autowired
    private UserAccountDao userAccountDao;

    @Autowired
    private SecurityUtils sut;

    private UserAccount user;

    @BeforeEach
    void setUp() {
        this.user = Generator.generateUserAccountWithPassword();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUserReturnsCurrentlyLoggedInUser() {
        Environment.setCurrentUser(user);
        final UserAccount result = sut.getCurrentUser();
        assertEquals(user, result);
    }

    @Test
    void getCurrentUserDetailsReturnsUserDetailsOfCurrentlyLoggedInUser() {
        Environment.setCurrentUser(user);
        final Optional<SGoVUserDetails> result = sut.getCurrentUserDetails();
        assertTrue(result.isPresent());
        assertTrue(result.get().isEnabled());
        assertEquals(user, result.get().getUser());
    }

    @Test
    void getCurrentUserDetailsReturnsEmptyOptionalWhenNoUserIsLoggedIn() {
        assertFalse(sut.getCurrentUserDetails().isPresent());
    }

    @Test
    void updateCurrentUserReplacesUserInCurrentSecurityContext() {
        Environment.setCurrentUser(user);
        final UserAccount update = new UserAccount();
        update.setUri(Generator.generateUri());
        update.setFirstName("updatedFirstName");
        update.setLastName("updatedLastName");
        update.setPassword(user.getPassword());
        update.setUsername(user.getUsername());
        transactional(() -> userAccountDao.update(update));
        sut.updateCurrentUser();

        final UserAccount currentUser = sut.getCurrentUser();
        assertEquals(update, currentUser);
    }

    @Test
    void verifyCurrentUserPasswordThrowsIllegalArgumentWhenPasswordDoesNotMatch() {
        Environment.setCurrentUser(user);
        final String password = "differentPassword";
        final ValidationException ex = assertThrows(ValidationException.class,
            () -> sut.verifyCurrentUserPassword(password));
        assertThat(ex.getMessage(), containsString("does not match"));
    }

    @Test
    void isAuthenticatedReturnsFalseForUnauthenticatedUser() {
        assertFalse(sut.isAuthenticated());
    }

    @Test
    void isAuthenticatedReturnsTrueForAuthenticatedUser() {
        Environment.setCurrentUser(user);
        assertTrue(sut.isAuthenticated());
    }

    @Test
    void isAuthenticatedReturnsFalseForAnonymousRequest() {
        final AnonymousAuthenticationToken token =
            new AnonymousAuthenticationToken("anonymousUser", "anonymousUser",
                Collections.singleton(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
        SecurityContextHolder.setContext(new SecurityContextImpl(token));
        assertFalse(sut.isAuthenticated());
    }

    @Test
    void isAuthenticatedWorksInStaticVersion() {
        Environment.setCurrentUser(user);
        assertTrue(SecurityUtils.authenticated());
    }
}