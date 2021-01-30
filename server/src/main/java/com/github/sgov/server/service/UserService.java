package com.github.sgov.server.service;

import com.github.sgov.server.model.UserAccount;
import com.github.sgov.server.service.security.SecurityUtils;
import org.springframework.stereotype.Service;

/**
 * User account-related business logic.
 */
@Service
public class UserService {

    /**
     * Retrieves currently logged in user.
     *
     * @return Currently logged in user's account
     */
    public UserAccount getCurrent() {
        final UserAccount account = SecurityUtils.getCurrentUser();
        account.erasePassword();
        return account;
    }
}
