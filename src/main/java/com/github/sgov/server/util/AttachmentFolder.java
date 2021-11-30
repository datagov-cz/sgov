package com.github.sgov.server.util;

import java.io.File;

public class AttachmentFolder extends Folder {

    public AttachmentFolder(File folder) {
        super(folder);
    }

    public File getAttachmentFile() {
        return getFile("příloha", "");
    }
}
