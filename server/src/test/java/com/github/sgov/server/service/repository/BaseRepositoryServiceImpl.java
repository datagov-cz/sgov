package com.github.sgov.server.service.repository;

import com.github.sgov.server.dao.GenericDao;
import com.github.sgov.server.dao.UserAccountDao;
import com.github.sgov.server.model.UserAccount;
import javax.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;

public class BaseRepositoryServiceImpl extends BaseRepositoryService<UserAccount> {

    private final UserAccountDao userAccountDao;

    @Autowired
    public BaseRepositoryServiceImpl(UserAccountDao userAccountDao, Validator validator) {
        super(validator);
        this.userAccountDao = userAccountDao;
    }

    @Override
    protected GenericDao<UserAccount> getPrimaryDao() {
        return userAccountDao;
    }
}
