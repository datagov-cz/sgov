package com.github.sgov.server.config.conf;

import com.github.sgov.server.config.conf.components.ComponentsConf;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.Transient;

@Setter
@Getter
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("repository")
@SuppressWarnings("checkstyle:MissingJavadocType")
public class RepositoryConf {

    private ComponentsConf componentsConf;

    /**
     * URL of the release SPARQL endpoint.
     */
    private String releaseSparqlEndpointUrl;

    /**
     * URL of the workspace repository.
     */
    private String url;

    private String username;

    private String password;

    private String githubRepo;

    private String githubOrganization;

    private String githubUserToken;

    @Transient
    private String remoteUrl;

    @Autowired
    public RepositoryConf(ComponentsConf componentsConf) {
        this.componentsConf = componentsConf;
    }

    /**
     * Returns repository.url. If empty, then returns components.dbServer.url
     *
     * @return url
     */
    public String getUrl() {
        if (!Strings.isEmpty(url)) {
            return url;
        }
        return componentsConf.getDbServerUrl();
    }

    public String getRemoteUrl() {
        return "https://github.com/" + getGithubOrganization() + "/" + getGithubRepo();
    }
}
