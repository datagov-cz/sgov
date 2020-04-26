package com.github.sgov.server.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.sgov.server.config.conf.UserConf;
import com.github.sgov.server.environment.Generator;
import com.github.sgov.server.model.UserAccount;
import com.github.sgov.server.service.repository.UserRepositoryService;
import com.github.sgov.server.util.Vocabulary;
import cz.cvut.kbss.jopa.model.EntityManager;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.List;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

class SystemInitializerTest extends BaseServiceTestRunner {

  private static final URI ADMIN_URI = URI.create(SystemInitializer.SYSTEM_ADMIN_USER_URI);

  @Autowired
  private UserConf userConf;

  @Autowired
  private UserRepositoryService userService;

  @Autowired
  private EntityManager em;

  @Autowired
  private PasswordEncoder passwordEncoder;

  private String adminCredentialsDir;

  private SystemInitializer sut;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
    // Randomize admin credentials folder
    this.adminCredentialsDir =
        System.getProperty("java.io.tmpdir") + File.separator + Generator.randomInt(0, 10000);
    userConf.setAdminCredentialsLocation(adminCredentialsDir);
    this.sut = new SystemInitializer(userConf, userService, txManager);
  }

  @AfterEach
  void tearDown() throws Exception {
    final File dir = new File(adminCredentialsDir);
    if (dir.listFiles() != null) {
      for (File child : dir.listFiles()) {
        Files.deleteIfExists(child.toPath());
      }
    }
    Files.deleteIfExists(dir.toPath());
  }

  @Test
  void persistsSystemAdminWhenHeDoesNotExist() {
    sut.initSystemAdmin();
    assertNotNull(em.find(UserAccount.class, ADMIN_URI));
  }

  @Test
  void doesNotCreateNewAdminWhenOneAlreadyExists() {
    sut.initSystemAdmin();
    final UserAccount admin = em.find(UserAccount.class, ADMIN_URI);
    sut.initSystemAdmin();
    final UserAccount result = em.find(UserAccount.class, ADMIN_URI);
    // We know that password is generated, so the same password means no new instance was created
    Assert.assertEquals(admin.getPassword(), result.getPassword());
  }

  @Test
  void savesAdminLoginCredentialsIntoHiddenFileInUserHome() throws Exception {
    sut.initSystemAdmin();
    final UserAccount admin = em.find(UserAccount.class, ADMIN_URI);
    final File credentialsFile = new File(userConf.getAdminCredentialsLocation()
        + File.separator + userConf.getAdminCredentialsFile());
    assertTrue(credentialsFile.exists());
    assertTrue(credentialsFile.isHidden());
    verifyAdminCredentialsFileContent(admin, credentialsFile);
  }

  private void verifyAdminCredentialsFileContent(UserAccount admin, File credentialsFile)
      throws IOException {
    final List<String> lines = Files.readAllLines(credentialsFile.toPath());
    assertThat(lines.get(0), containsString(admin.getUsername() + "/"));
    final String password = lines.get(0).substring(lines.get(0).indexOf('/') + 1);
    assertTrue(passwordEncoder.matches(password, admin.getPassword()));
  }

  @Test
  void savesAdminLoginCredentialsIntoConfiguredFile() throws Exception {
    final String adminFileName = ".admin-file-with-different-name";
    userConf.setAdminCredentialsFile(adminFileName);
    this.sut = new SystemInitializer(userConf, userService, txManager);
    sut.initSystemAdmin();
    final UserAccount admin = em.find(UserAccount.class, ADMIN_URI);
    final File credentialsFile = new File(adminCredentialsDir + File.separator + adminFileName);
    assertTrue(credentialsFile.exists());
    assertTrue(credentialsFile.isHidden());
    verifyAdminCredentialsFileContent(admin, credentialsFile);
  }

  @Test
  void ensuresGeneratedAccountIsAdmin() {
    sut.initSystemAdmin();
    final UserAccount result = em.find(UserAccount.class, ADMIN_URI);
    assertNotNull(result);
    assertThat(result.getTypes(), hasItem(Vocabulary.s_c_administrator));
    assertThat(result.getTypes(), not(hasItem(Vocabulary.s_c_omezeny_uzivatel)));
  }
}
