package com.github.sgov.server.service.repository;

import com.github.sgov.server.dao.AttachmentDao;
import com.github.sgov.server.dao.GenericDao;
import com.github.sgov.server.model.AttachmentContext;
import java.net.URI;
import javax.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Service to managed attachments.
 */
@Service
public class AttachmentRepositoryService extends BaseRepositoryService<AttachmentContext> {

    private final AttachmentDao attachmentDao;

    @Autowired
    public AttachmentRepositoryService(@Qualifier("validatorFactoryBean") Validator validator,
                                       AttachmentDao attachmentDao) {
        super(validator);
        this.attachmentDao = attachmentDao;
    }

    @Override
    protected GenericDao<AttachmentContext> getPrimaryDao() {
        return attachmentDao;
    }

    @Override
    public void remove(AttachmentContext instance) {
        super.remove(instance);
        clearAttachmentContext(instance.getUri());
    }

    @Override
    public void remove(URI id) {
        super.remove(id);
        clearAttachmentContext(id);
    }

    /**
     * Clears the given attachment context.
     *
     * @param attachmentContext attachmentContext
     */
    public void clearAttachmentContext(final URI attachmentContext) {
        attachmentDao.clearAttachmentContext(attachmentContext);
    }
}
