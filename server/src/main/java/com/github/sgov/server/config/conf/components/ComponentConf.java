package com.github.sgov.server.config.conf.components;

import java.util.Map;

public class ComponentConf {

    private String url;

    private String name;

    private Map<?, ?> meta;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<?, ?> getMeta() {
        return meta;
    }

    public void setMeta(Map<?, ?> meta) {
        this.meta = meta;
    }
}
