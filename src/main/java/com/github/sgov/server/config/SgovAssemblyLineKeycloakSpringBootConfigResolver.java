package com.github.sgov.server.config;

import com.github.sgov.server.config.conf.components.ComponentsProperties;
import org.apache.logging.log4j.util.Strings;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.OIDCHttpFacade;
import org.keycloak.representations.adapters.config.AdapterConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SgovAssemblyLineKeycloakSpringBootConfigResolver
    implements org.keycloak.adapters.KeycloakConfigResolver {

    private KeycloakDeployment keycloakDeployment;

    @Autowired
    private ComponentsProperties componentsProperties;

    @Autowired(required = false)
    private AdapterConfig adapterConfig;

    @Override
    public KeycloakDeployment resolve(OIDCHttpFacade.Request request) {
        if (keycloakDeployment != null) {
            return keycloakDeployment;
        }
        if (Strings.isEmpty(adapterConfig.getAuthServerUrl())) {
            final String authServerUrl = componentsProperties.getComponents().getAuthServerUrl();
            adapterConfig
                .setAuthServerUrl(authServerUrl.substring(0, authServerUrl.indexOf("realms")));
        }

        keycloakDeployment = KeycloakDeploymentBuilder.build(adapterConfig);

        return keycloakDeployment;
    }
}
