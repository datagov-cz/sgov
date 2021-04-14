package com.github.sgov.server.util;

import java.net.URISyntaxException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class IdnUtilsTest {

    @Test
    public void convertUnicodeUrlToAsciiReturnsTheSameUrlForAsciiInput() throws URISyntaxException {
        final String url = "https://google.com/";
        Assertions.assertEquals(url,IdnUtils.convertUnicodeUrlToAscii(url));
    }

    @Test
    public void convertUnicodeUrlToAsciiReturnsTheAceUrlForUnicodeInput() throws URISyntaxException {
        final String url = "https://slovník.gov.cz/sparql";
        final String aceUrl = "https://xn--slovnk-7va.gov.cz/sparql";
        Assertions.assertEquals(aceUrl,IdnUtils.convertUnicodeUrlToAscii(url));
    }
}