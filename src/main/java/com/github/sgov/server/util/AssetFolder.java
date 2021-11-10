package com.github.sgov.server.util;

import java.io.File;

public class AssetFolder extends Folder {

    public AssetFolder(File folder) {
        super(folder);
    }

    public File getAssetFile() {
        return getFile("příloha", "");
    }
}
