package com.github.sgov.server.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.sgov.server.environment.Generator;
import com.github.sgov.server.util.Vocabulary;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserAccountTest {

  private UserAccount sut;

  @BeforeEach
  void setUp() {
    this.sut = Generator.generateUserAccount();
  }

  @Test
  void toUserReturnsUserWithIdenticalAttributes() {
    final UserAccount ua = Generator.generateUserAccount();
    ua.setTypes(Collections.singleton(Vocabulary.s_c_administrator));

    final User result = ua.toUser();
    assertAll(() -> assertEquals(ua.getUri(), result.getUri()),
        () -> assertEquals(ua.getFirstName(), result.getFirstName()),
        () -> assertEquals(ua.getLastName(), result.getLastName()),
        () -> assertEquals(ua.getUsername(), result.getUsername()),
        () -> assertEquals(ua.getTypes(), result.getTypes()));
  }

  @Test
  void erasePasswordRemovesPasswordFromInstance() {
    sut.setPassword("test");
    sut.erasePassword();
    assertNull(sut.getPassword());
  }

  @Test
  void isLockedReturnsFalseForNonLockedInstance() {
    assertFalse(sut.isLocked());
  }

  @Test
  void isLockedReturnsTrueForLockedInstance() {
    sut.addType(Vocabulary.s_c_uzamceny_uzivatel);
    assertTrue(sut.isLocked());
  }

  @Test
  void lockSetsLockedStatusOfInstance() {
    assertFalse(sut.isLocked());
    sut.lock();
    assertTrue(sut.isLocked());
  }

  @Test
  void unlockRemovesLockedStatusOfInstance() {
    sut.lock();
    assertTrue(sut.isLocked());
    sut.unlock();
    assertFalse(sut.isLocked());
  }

  @Test
  void disableAddsDisabledTypeToInstance() {
    assertTrue(sut.isEnabled());
    sut.disable();
    assertFalse(sut.isEnabled());
  }

  @Test
  void enableRemovesDisabledTypeFromInstance() {
    sut.addType(Vocabulary.s_c_zablokovany_uzivatel);
    assertFalse(sut.isEnabled());
    sut.enable();
    assertTrue(sut.isEnabled());
  }

  @Test
  void removeTypeHandlesNullTypesAttribute() {
    sut.removeType(Vocabulary.s_c_administrator);
  }

  @Test
  void isAdminReturnsTrueForAdmin() {
    assertFalse(sut.isAdmin());
    sut.addType(Vocabulary.s_c_administrator);
    assertTrue(sut.isAdmin());
  }
}
