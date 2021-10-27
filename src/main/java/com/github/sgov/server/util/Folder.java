package com.github.sgov.server.util;

import java.io.File;

public abstract class Folder {

    private final File folder;

    public Folder(File folder) {
        this.folder = folder;
    }

    private String getId() {
        final String p = folder.getAbsolutePath();
        return p.substring(p.lastIndexOf("/") + 1);
    }

    protected File getFile(String type, String suffix) {
        return new File(folder,
            getId() + "-" + type + (suffix.isEmpty() ? "" : "-" + suffix)
                + Constants.Turtle.FILE_EXTENSION);
    }

    public File getFolder() {
        return folder;
    }
}
