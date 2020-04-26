package com.github.sgov.server.persistence.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.sgov.server.dao.UserAccountDao;
import com.github.sgov.server.environment.Generator;
import com.github.sgov.server.model.UserAccount;
import cz.cvut.kbss.jopa.model.EntityManager;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@Tag("dao")
@ContextConfiguration(classes = {UserAccountDao.class})
class UserAccountDaoTest extends BaseDaoTestRunner {

  @Autowired
  private EntityManager em;

  @Autowired
  private UserAccountDao sut;

  @Test
  void findByUsernameReturnsMatchingUser() {
    final UserAccount user = Generator.generateUserAccountWithPassword();
    transactional(() -> em.persist(user));

    final Optional<UserAccount> result = sut.findByUsername(user.getUsername());
    assertTrue(result.isPresent());
    assertEquals(user, result.get());
  }

  @Test
  void findByUsernameReturnsEmptyOptionalWhenNoMatchingUserIsFound() {
    final Optional<UserAccount> result = sut.findByUsername("unknown@kbss.felk.cvut.cz");
    assertNotNull(result);
    assertFalse(result.isPresent());
  }

  @Test
  void existsByUsernameReturnsTrueForExistingUsername() {
    final UserAccount user = Generator.generateUserAccountWithPassword();
    transactional(() -> em.persist(user));

    assertTrue(sut.exists(user.getUsername()));
  }

  @Test
  void existsByUsernameReturnsFalseForUnknownUsername() {
    assertFalse(sut.exists("unknownUsername"));
  }

  @Test
  void findAllReturnsAccountsSortedByUserLastNameAndFirstName() {
    final List<UserAccount> accounts = IntStream.range(0, 10)
        .mapToObj(i -> Generator.generateUserAccountWithPassword()).collect(
            Collectors.toList());
    transactional(() -> accounts.forEach(em::persist));

    final List<UserAccount> result = sut.findAll();
    accounts.sort(
        Comparator.comparing(UserAccount::getLastName).thenComparing(UserAccount::getFirstName));
    assertEquals(result, accounts);
  }
}
