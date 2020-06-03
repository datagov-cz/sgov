package com.github.sgov.server.util;

import com.google.common.base.CharMatcher;
import java.net.IDN;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Utility functions for working with IDN.
 */
public class IdnUtils {

    /**
     * Converts a unicode Url to ACE.
     *
     * @param url url with possibly unicode characters
     * @return ACE version of input
     * @throws URISyntaxException an exception raised if URL us invalid.
     */
    public static String convertUnicodeUrlToAscii(String url) throws URISyntaxException {
        if (url == null) {
            return null;
        }
        url = url.trim();
        // Handle international domains by detecting non-ascii and converting them to punycode
        boolean isAscii = CharMatcher.ascii().matchesAllOf(url);
        if (!isAscii) {
            URI uri = new URI(url);
            boolean includeScheme = true;

            // URI needs a scheme to work properly with authority parsing
            if (uri.getScheme() == null) {
                uri = new URI("http://" + url);
                includeScheme = false;
            }

            final String scheme = uri.getScheme() != null ? uri.getScheme() + "://" : null;
            final String authority = uri.getRawAuthority() != null ? uri.getRawAuthority() :
                ""; // includes domain and port
            final String path = uri.getRawPath() != null ? uri.getRawPath() : "";
            final String queryString = uri.getRawQuery() != null ? "?" + uri.getRawQuery() : "";

            // Must convert domain to punycode separately from the path
            url = (includeScheme ? scheme : "") + IDN.toASCII(authority) + path + queryString;

            // Convert path from unicode to ascii encoding
            url = new URI(url).toASCIIString();
        }
        return url;
    }
}
