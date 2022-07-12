package com.github.sgov.server.service.repository;

import com.github.sgov.server.dao.AttachmentDao;
import com.github.sgov.server.dao.GenericDao;
import com.github.sgov.server.model.AttachmentContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.validation.Validator;

/**
 * Service to managed attachments.
 */
@Service
public class AttachmentRepositoryService extends BaseRepositoryService<AttachmentContext> {

    private final AttachmentDao attachmentDao;

    @Autowired
    public AttachmentRepositoryService(@Qualifier("validatorFactoryBean")Validator validator, AttachmentDao attachmentDao) {
        super(validator);
        this.attachmentDao = attachmentDao;
    }

    @Override
    protected GenericDao<AttachmentContext> getPrimaryDao() {
        return attachmentDao;
    }
}
