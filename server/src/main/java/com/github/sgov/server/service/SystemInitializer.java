package com.github.sgov.server.service;

import com.github.sgov.server.config.conf.UserConf;
import com.github.sgov.server.model.UserAccount;
import com.github.sgov.server.service.repository.UserRepositoryService;
import com.github.sgov.server.util.Vocabulary;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Random;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

public class SystemInitializer {

  public static final String SYSTEM_ADMIN_USER_URI =
      "https://slovník.gov.cz/aplikační/výrobní-linka/slovník/system-admin-user";

  private static final Logger LOG = LoggerFactory.getLogger(SystemInitializer.class);

  private static final String LETTERS = "abcdefghijklmnopqrstuvwxyz";
  private static final int PASSWORD_LENGTH = 8;

  private final UserConf config;
  private final UserRepositoryService userService;
  private final PlatformTransactionManager txManager;

  /**
   * SystemInitializer.
   */
  @Autowired
  public SystemInitializer(UserConf config, UserRepositoryService userService,
                           PlatformTransactionManager txManager) {
    this.config = config;
    this.userService = userService;
    this.txManager = txManager;
  }

  private static UserAccount initAdminInstance() {
    final UserAccount admin = new UserAccount();
    admin.setUri(URI.create(SYSTEM_ADMIN_USER_URI));
    admin.setFirstName("System");
    admin.setLastName("Administrator");
    admin.setUsername("termit-admin@kbss.felk.cvut.cz");
    admin.addType(Vocabulary.s_c_administrator);
    return admin;
  }

  private static String generatePassword() {
    final StringBuilder sb = new StringBuilder(PASSWORD_LENGTH);
    final Random random = new Random();
    for (int i = 0; i < PASSWORD_LENGTH; i++) {
      if (random.nextBoolean()) {
        sb.append(random.nextInt(10));
      } else {
        char c = LETTERS.charAt(random.nextInt(LETTERS.length()));
        sb.append(random.nextBoolean() ? c : Character.toUpperCase(c));
      }
    }
    return sb.toString();
  }

  @PostConstruct
  void initSystemAdmin() {    // Package-private for testing purposes
    final UserAccount admin = initAdminInstance();
    final String passwordPlain = generatePassword();
    admin.setPassword(passwordPlain);
    if (userService.exists(admin.getUri())) {
      LOG.info("Admin already exists.");
      return;
    }
    LOG.info("Creating application admin account.");
    final TransactionTemplate tx = new TransactionTemplate(txManager);
    tx.execute(status -> {
      userService.persist(admin);
      return null;
    });
    LOG.info("----------------------------------------------");
    LOG.info("Admin credentials are: {}/{}", admin.getUsername(), passwordPlain);
    LOG.info("----------------------------------------------");
    final File directory = new File(config.getAdminCredentialsLocation());
    try {
      if (!directory.exists()) {
        Files.createDirectories(directory.toPath());
      }
      final File credentialsFile = createHiddenFile();
      if (credentialsFile == null) {
        return;
      }
      LOG.debug("Writing admin credentials into file: {}", credentialsFile);
      Files.write(credentialsFile.toPath(),
          Collections.singletonList(admin.getUsername() + "/" + passwordPlain),
          StandardOpenOption.CREATE, StandardOpenOption.WRITE,
          StandardOpenOption.TRUNCATE_EXISTING);
    } catch (IOException e) {
      LOG.error("Unable to create admin credentials file.", e);
    }
  }

  private File createHiddenFile() throws IOException {
    final File credentialsFile = new File(config.getAdminCredentialsLocation() + File.separator
        + config.getAdminCredentialsFile());
    final boolean result = credentialsFile.createNewFile();
    if (!result) {
      LOG.error(
          "Unable to create admin credentials file {}. Admin credentials won't be saved in any "
              + "file!",
          config.getAdminCredentialsFile());
      return null;
    }
    // Hidden attribute on Windows
    Files.setAttribute(credentialsFile.toPath(), "dos:hidden", Boolean.TRUE,
        LinkOption.NOFOLLOW_LINKS);
    return credentialsFile;
  }
}
