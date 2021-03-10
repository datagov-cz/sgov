package com.github.sgov.server.config.conf.components;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class ComponentsConf {

    public static final String DB_SERVER = "dbServer";

    public static final String AUTH_SERVER = "authServer";

    Map<String, ComponentConf> components;

    public ComponentsConf(
        Map<String, ComponentConf> components) {
        this.components = components;
    }

    public String getDbServerUrl() {
        return getComponents().get(ComponentsConf.DB_SERVER).getUrl();
    }

    public String getAuthServerUrl() {
        return getComponents().get(ComponentsConf.AUTH_SERVER).getUrl();
    }
}
