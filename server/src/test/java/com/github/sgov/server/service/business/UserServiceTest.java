package com.github.sgov.server.service.business;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.sgov.server.controller.dto.UserUpdateDto;
import com.github.sgov.server.environment.Generator;
import com.github.sgov.server.event.LoginAttemptsThresholdExceeded;
import com.github.sgov.server.exception.AuthorizationException;
import com.github.sgov.server.exception.ValidationException;
import com.github.sgov.server.model.UserAccount;
import com.github.sgov.server.service.UserService;
import com.github.sgov.server.service.repository.UserRepositoryService;
import com.github.sgov.server.service.security.SecurityUtils;
import com.github.sgov.server.util.Vocabulary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

class UserServiceTest {

    @Mock
    private UserRepositoryService repositoryServiceMock;

    @Mock
    private SecurityUtils securityUtilsMock;

    @InjectMocks
    private UserService sut;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void findAllLoadsUsersFromRepositoryService() {
        sut.findAll();
        verify(repositoryServiceMock).findAll();
    }

    @Test
    void persistPassesAccountToPersistToRepositoryService() {
        final UserAccount toPersist = Generator.generateUserAccount();
        sut.persist(toPersist);
        verify(repositoryServiceMock).persist(toPersist);
    }

    @Test
    void updateVerifiesOriginalPasswordBeforeUpdatingAccountWithNewPassword() {
        final UserUpdateDto update = new UserUpdateDto();
        update.setUri(Generator.generateUri());
        update.setFirstName("firstName");
        update.setLastName("lastName");
        update.setUsername("username");
        update.setPassword("password");
        update.setOriginalPassword("originalPassword");
        when(securityUtilsMock.getCurrentUser()).thenReturn(update.asUserAccount());
        sut.updateCurrent(update);
        final InOrder inOrder = Mockito.inOrder(securityUtilsMock, repositoryServiceMock);
        inOrder.verify(securityUtilsMock).verifyCurrentUserPassword(update.getOriginalPassword());
        inOrder.verify(repositoryServiceMock).update(update.asUserAccount());
    }

    @Test
    void updateDoesNotVerifyOriginalPasswordWhenAccountDoesNotUpdatePassword() {
        final UserUpdateDto update = new UserUpdateDto();
        update.setUri(Generator.generateUri());
        update.setFirstName("firstName");
        update.setLastName("lastName");
        update.setUsername("username");
        when(securityUtilsMock.getCurrentUser()).thenReturn(update.asUserAccount());
        sut.updateCurrent(update);
        verify(repositoryServiceMock).update(update.asUserAccount());
        verify(securityUtilsMock, never()).verifyCurrentUserPassword(any());
    }

    @Test
    void updateThrowsAuthorizationExceptionWhenAttemptingToUpdateDifferentUserThatCurrent() {
        final UserUpdateDto update = new UserUpdateDto();
        update.setUri(Generator.generateUri());
        update.setFirstName("firstName");
        update.setLastName("lastName");
        update.setUsername("username");
        final UserAccount ua = Generator.generateUserAccount();
        when(securityUtilsMock.getCurrentUser()).thenReturn(ua);
        assertThrows(AuthorizationException.class, () -> sut.updateCurrent(update));
    }

    @Test
    void updateThrowsValidationExceptionWhenAttemptingToUpdateUsernameOfAccount() {
        final UserAccount ua = Generator.generateUserAccount();
        final UserUpdateDto update = new UserUpdateDto();

        update.setUri(ua.getUri());
        update.setUsername("username");

        when(securityUtilsMock.getCurrentUser()).thenReturn(ua);
        assertThrows(ValidationException.class, () -> sut.updateCurrent(update));
    }

    @Test
    void existsChecksForUsernameExistenceInRepositoryService() {
        final String username = "user@termit";
        sut.exists(username);
        verify(repositoryServiceMock).exists(username);
    }

    @Test
    void unlockUnlocksUserAccountAndUpdatesItViaRepositoryService() {
        when(securityUtilsMock.getCurrentUser()).thenReturn(Generator.generateUserAccount());
        final UserAccount account = Generator.generateUserAccount();
        account.lock();
        sut.unlock(account, "newPassword");
        final ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
        verify(repositoryServiceMock).update(captor.capture());
        assertFalse(captor.getValue().isLocked());
    }

    @Test
    void unlockSetsNewPasswordOnAccountSpecifiedAsArgument() {
        when(securityUtilsMock.getCurrentUser()).thenReturn(Generator.generateUserAccount());
        final UserAccount account = Generator.generateUserAccount();
        account.lock();
        final String newPassword = "newPassword";
        sut.unlock(account, newPassword);
        final ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
        verify(repositoryServiceMock).update(captor.capture());
        assertEquals(newPassword, captor.getValue().getPassword());
    }

    @Test
    void unlockThrowsUnsupportedOperationExceptionWhenAttemptingToUnlockOwnAccount() {
        final UserAccount account = Generator.generateUserAccount();
        account.lock();
        when(securityUtilsMock.getCurrentUser()).thenReturn(account);
        assertThrows(UnsupportedOperationException.class, () -> sut.unlock(account, "newPassword"));
        verify(repositoryServiceMock, never()).update(any());
    }

    @Test
    void disableDisablesUserAccountAndUpdatesItViaRepositoryService() {
        when(securityUtilsMock.getCurrentUser()).thenReturn(Generator.generateUserAccount());
        final UserAccount account = Generator.generateUserAccount();
        sut.disable(account);
        final ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
        verify(repositoryServiceMock).update(captor.capture());
        assertFalse(captor.getValue().isEnabled());
    }

    @Test
    void disableThrowsUnsupportedOperationExceptionWhenAttemptingToDisableOwnAccount() {
        final UserAccount account = Generator.generateUserAccount();
        when(securityUtilsMock.getCurrentUser()).thenReturn(account);
        assertThrows(UnsupportedOperationException.class, () -> sut.disable(account));
        verify(repositoryServiceMock, never()).update(any());
    }

    @Test
    void enableEnablesUserAccountAndUpdatesItViaRepositoryService() {
        when(securityUtilsMock.getCurrentUser()).thenReturn(Generator.generateUserAccount());
        final UserAccount account = Generator.generateUserAccount();
        account.disable();
        sut.enable(account);
        final ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
        verify(repositoryServiceMock).update(captor.capture());
        assertTrue(captor.getValue().isEnabled());
    }

    @Test
    void enableThrowsUnsupportedOperationExceptionWhenAttemptingToEnableOwnAccount() {
        final UserAccount account = Generator.generateUserAccount();
        account.disable();
        when(securityUtilsMock.getCurrentUser()).thenReturn(account);
        assertThrows(UnsupportedOperationException.class, () -> sut.enable(account));
        verify(repositoryServiceMock, never()).update(any());
    }

    @Test
    void onLoginAttemptsThresholdExceededLocksUserAccountAndUpdatesItViaRepositoryService() {
        final UserAccount account = Generator.generateUserAccount();
        sut.onLoginAttemptsThresholdExceeded(new LoginAttemptsThresholdExceeded(account));
        final ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
        verify(repositoryServiceMock).update(captor.capture());
        assertTrue(captor.getValue().isLocked());
    }

    @Test
    void getCurrentRetrievesCurrentlyLoggedInUserAccount() {
        final UserAccount account = Generator.generateUserAccount();
        when(securityUtilsMock.getCurrentUser()).thenReturn(account);
        final UserAccount result = sut.getCurrent();
        assertEquals(account, result);
    }

    @Test
    void getCurrentReturnsCurrentUserAccountWithoutPassword() {
        final UserAccount account = Generator.generateUserAccount();
        account.setPassword("12345");
        when(securityUtilsMock.getCurrentUser()).thenReturn(account);
        final UserAccount result = sut.getCurrent();
        assertNull(result.getPassword());
    }

    @Test
    void persistAddsUserRestrictedType() {
        final UserAccount user = Generator.generateUserAccountWithPassword();
        sut.persist(user);

        final ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
        verify(repositoryServiceMock).persist(captor.capture());
        assertThat(captor.getValue().getTypes(), hasItem(Vocabulary.s_c_omezeny_uzivatel));
    }

    @Test
    void persistEnsuresAdminTypeIsNotPresentInUserAccount() {
        final UserAccount user = Generator.generateUserAccountWithPassword();
        user.addType(Vocabulary.s_c_administrator);
        sut.persist(user);

        final ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
        verify(repositoryServiceMock).persist(captor.capture());
        assertThat(captor.getValue().getTypes(), not(hasItem(Vocabulary.s_c_administrator)));
    }

    @Test
    void persistDoesNotRestrictUserTypeIfItIsBeingPersistedByAdmin() {
        final UserAccount currentUser = Generator.generateUserAccountWithPassword();
        currentUser.addType(Vocabulary.s_c_administrator);
        when(securityUtilsMock.isAuthenticated()).thenReturn(true);
        when(securityUtilsMock.getCurrentUser()).thenReturn(currentUser);
        final UserAccount user = Generator.generateUserAccountWithPassword();
        user.addType(Vocabulary.s_c_administrator);
        sut.persist(user);

        final ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
        verify(repositoryServiceMock).persist(captor.capture());
        assertThat(captor.getValue().getTypes(), hasItem(Vocabulary.s_c_administrator));
        assertThat(captor.getValue().getTypes(), not(hasItem(Vocabulary.s_c_omezeny_uzivatel)));
    }

    @Test
    void persistFailsForUsernameWhichIsNotAnEmailAddress() {
        final UserAccount user = Generator.generateUserAccountWithPassword();
        user.setUsername("non-email-username");
        assertThrows(ValidationException.class, () -> sut.persist(user));
    }
}
