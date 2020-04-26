package com.github.sgov.server.controller;

import static com.github.sgov.server.environment.Generator.generateUserAccount;
import static com.github.sgov.server.service.IdentifierResolver.extractIdentifierFragment;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.sgov.server.controller.dto.UserUpdateDto;
import com.github.sgov.server.environment.Environment;
import com.github.sgov.server.model.UserAccount;
import com.github.sgov.server.service.IdentifierResolver;
import com.github.sgov.server.service.UserService;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

class UserControllerTest extends BaseControllerTestRunner {

  private static final String BASE_URL = "/users";

  @Mock
  private UserService userService;

  @Mock
  private IdentifierResolver idResolverMock;

  @InjectMocks
  private UserController sut;

  private UserAccount user;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
    super.setUp(sut);
    this.user = generateUserAccount();
    Environment.setCurrentUser(user);
  }

  @Test
  void getAllReturnsAllUsers() throws Exception {
    final List<UserAccount> users = IntStream.range(0, 5).mapToObj(i -> generateUserAccount())
        .collect(Collectors.toList());
    when(userService.findAll()).thenReturn(users);

    final MvcResult mvcResult =
        mockMvc.perform(get(BASE_URL).accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk()).andReturn();
    final List<UserAccount> result = readValue(mvcResult, new TypeReference<List<UserAccount>>() {
    });
    assertEquals(users, result);
  }

  @Test
  void updateCurrentSendsUserUpdateToService() throws Exception {
    final UserUpdateDto dto = dtoForUpdate();

    mockMvc.perform(
        put(BASE_URL + "/current").content(toJson(dto))
            .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isNoContent());
    verify(userService).updateCurrent(dto);
  }

  private UserUpdateDto dtoForUpdate() {
    final UserUpdateDto dto = new UserUpdateDto();
    dto.setUri(user.getUri());
    dto.setFirstName(user.getFirstName());
    dto.setLastName(user.getLastName());
    dto.setPassword("newPassword");
    dto.setUsername(user.getUsername());
    dto.setOriginalPassword(user.getPassword());
    return dto;
  }

  @Test
  void unlockUnlocksUser() throws Exception {
    final String newPassword = "newPassword";

    when(idResolverMock.resolveUserIdentifier(any())).thenReturn(user.getUri());
    when(userService.findRequired(user.getUri())).thenReturn(user);
    mockMvc.perform(delete(BASE_URL + "/" + extractIdentifierFragment(user.getUri()) + "/lock")
        .content(newPassword))
        .andExpect(status().isNoContent());
    verify(userService).unlock(user, newPassword);
  }

  @Test
  void enableEnablesUser() throws Exception {
    when(idResolverMock.resolveUserIdentifier(any())).thenReturn(user.getUri());
    when(userService.findRequired(user.getUri())).thenReturn(user);
    mockMvc.perform(post(BASE_URL + "/" + extractIdentifierFragment(user.getUri()) + "/status"))
        .andExpect(status().isNoContent());
    verify(userService).enable(user);
  }

  @Test
  void disableDisablesUser() throws Exception {
    when(idResolverMock.resolveUserIdentifier(any())).thenReturn(user.getUri());
    when(userService.findRequired(user.getUri())).thenReturn(user);
    mockMvc.perform(delete(BASE_URL + "/" + extractIdentifierFragment(user.getUri()) + "/status"))
        .andExpect(status().isNoContent());
    verify(userService).disable(user);
  }

  @Test
  void existsChecksForUsernameExistence() throws Exception {
    when(userService.exists(user.getUsername())).thenReturn(true);
    final MvcResult mvcResult =
        mockMvc.perform(get(BASE_URL + "/username").param("username", user.getUsername()))
            .andReturn();
    final Boolean result = readValue(mvcResult, Boolean.class);
    assertTrue(result);
  }
}
