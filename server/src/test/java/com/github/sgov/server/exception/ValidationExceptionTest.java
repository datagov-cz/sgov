package com.github.sgov.server.exception;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.sgov.server.model.UserAccount;
import com.github.sgov.server.util.ValidationResult;
import javax.validation.Validation;
import javax.validation.Validator;
import org.junit.jupiter.api.Test;

class ValidationExceptionTest {

  @Test
  void getMessageReturnsMessageWhenItWasSpecified() {
    final String msg = "I have a bad feeling about this.";
    final ValidationException ex = new ValidationException(msg);
    assertEquals(msg, ex.getMessage());
  }

  @Test
  void getMessageReturnsConcatenatedConstraintViolationMessages() {
    final UserAccount u = new UserAccount();
    u.setFirstName("test");
    u.setLastName("testowitch");
    final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    final ValidationResult<UserAccount> violations = ValidationResult.of(validator.validate(u));
    final ValidationException ex = new ValidationException(violations);
    final String result = ex.getMessage();
    assertAll(() -> assertThat(result, containsString("username")),
        () -> assertThat(result, containsString("password")));
  }
}