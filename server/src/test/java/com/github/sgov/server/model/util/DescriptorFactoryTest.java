package com.github.sgov.server.model.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.sgov.server.environment.Generator;
import com.github.sgov.server.model.UserAccount;
import cz.cvut.kbss.jopa.model.descriptors.Descriptor;
import cz.cvut.kbss.jopa.model.metamodel.FieldSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DescriptorFactoryTest {

  private UserAccount modelObject;

  private FieldSpecification passwordFieldSpec;

  @BeforeEach
  void setUp() {
    this.modelObject = Generator.generateUserAccount();
    this.passwordFieldSpec = mock(FieldSpecification.class);
    when(passwordFieldSpec.getJavaField()).thenReturn(UserAccount.getPasswordField());
  }

  @Test
  void userDescriptorCreatesSimpleUserDescriptor() {
    final Descriptor result = DescriptorFactory.userManagementDescriptor(modelObject);
    assertEquals(modelObject.getUri(), result.getContext());
    assertEquals(modelObject.getUri(), result.getAttributeContext(passwordFieldSpec));
  }
}
