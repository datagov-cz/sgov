package com.github.sgov.server.config;

import com.github.sgov.server.servlet.DiagnosticsContextFilter;
import java.util.Collections;
import java.util.EnumSet;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextListener;

/**
 * Entry point of the application invoked by the application server on deploy.
 */
@Component
public class DispatcherServletInitializer
    implements ServletContextInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(DispatcherServletInitializer.class);

    @Override
    public void onStartup(ServletContext servletContext) {
        printStartupMessage();
        initMdcFilter(servletContext);
        servletContext.addListener(new RequestContextListener());
    }

    private static void printStartupMessage() {
        final String msg = "* SGoV Server *";
        final String delimiter = String.join("", Collections.nCopies(msg.length(), "*"));
        LOG.info(delimiter);
        LOG.info(msg);
        LOG.info(delimiter);
    }

    /**
     * Initializes diagnostics context filter for logging session info.
     */
    private static void initMdcFilter(ServletContext servletContext) {
        FilterRegistration.Dynamic mdcFilter = servletContext
            .addFilter("diagnosticsContextFilter", new DiagnosticsContextFilter());
        final EnumSet<DispatcherType> es =
            EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD);
        mdcFilter.addMappingForUrlPatterns(es, true, "/*");
    }
}
