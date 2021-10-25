package com.github.sgov.server.servlet;

import java.io.IOException;
import java.security.Principal;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.web.filter.GenericFilterBean;

/**
 * Stores user info into the Mapped Diagnostic Context for the logging framework.
 */
public class DiagnosticsContextFilter extends GenericFilterBean {

    static final String MDC_KEY = "username";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain)
        throws IOException, ServletException {
        final HttpServletRequest req = (HttpServletRequest) servletRequest;
        final Principal principal = req.getUserPrincipal();
        boolean mdcSet = false;
        if (principal != null) {
            final String username = req.getUserPrincipal().getName();
            MDC.put(MDC_KEY, username);
            mdcSet = true;
        }

        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            if (mdcSet) {
                MDC.remove(MDC_KEY);
            }
        }
    }
}
