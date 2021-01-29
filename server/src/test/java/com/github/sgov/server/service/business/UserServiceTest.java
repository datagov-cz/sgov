package com.github.sgov.server.service.business;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


import com.github.sgov.server.environment.Generator;
import com.github.sgov.server.model.UserAccount;
import com.github.sgov.server.service.UserService;
import com.github.sgov.server.service.repository.UserRepositoryService;
import com.github.sgov.server.service.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
}
