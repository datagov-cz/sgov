package com.github.sgov.server.controller.dto;

import static org.junit.Assert.assertEquals;

import com.github.sgov.server.environment.Generator;
import com.github.sgov.server.model.UserAccount;
import org.junit.jupiter.api.Test;

class UserUpdateDtoTest {

  @Test
  void asUserAccountCopiesAllAttributesIntoNewUserInstance() {
    final UserAccount user = Generator.generateUserAccount();
    final UserUpdateDto dto = new UserUpdateDto();
    dto.setUri(user.getUri());
    dto.setFirstName(user.getFirstName());
    dto.setLastName(user.getLastName());
    dto.setUsername(user.getUsername());
    dto.setPassword(user.getPassword());
    dto.setTypes(user.getTypes());
    dto.setOriginalPassword("test");

    final UserAccount result = dto.asUserAccount();
    assertEquals(user.getUri(), result.getUri());
    assertEquals(user.getFirstName(), result.getFirstName());
    assertEquals(user.getLastName(), result.getLastName());
    assertEquals(user.getUsername(), result.getUsername());
    assertEquals(user.getPassword(), result.getPassword());
    assertEquals(user.getTypes(), result.getTypes());
  }
}