package com.github.sgov.server.controller.dto;

import com.github.sgov.server.model.UserAccount;
import com.github.sgov.server.util.Vocabulary;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import java.util.Objects;

/**
 * DTO used for user updating so that original password can be validated.
 */
@OWLClass(iri = Vocabulary.s_c_uzivatel)
public class UserUpdateDto extends UserAccount {

  @OWLDataProperty(iri = Vocabulary.s_p_ma_puvodni_heslo)
  private String originalPassword;

  public String getOriginalPassword() {
    return originalPassword;
  }

  public void setOriginalPassword(String originalPassword) {
    this.originalPassword = originalPassword;
  }

  /**
   * Transforms this DTO to the regular entity.
   *
   * <p>This is necessary for correct persistence processing, as this class is not a known entity
   * class.
   *
   * @return {@link UserAccount} instance
   */
  public UserAccount asUserAccount() {
    final UserAccount user = new UserAccount();
    user.setUri(getUri());
    user.setFirstName(getFirstName());
    user.setLastName(getLastName());
    user.setUsername(getUsername());
    user.setPassword(getPassword());
    user.setTypes(getTypes());
    return user;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof UserUpdateDto)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    final UserUpdateDto that = (UserUpdateDto) o;
    return Objects.equals(originalPassword, that.originalPassword);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), originalPassword);
  }
}
