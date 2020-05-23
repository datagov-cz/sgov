package com.github.sgov.server.util;

/**
 * Application-wide constants.
 */
public class Constants {

    public static final class Turtle {

        /**
         * Media type for RDF serialized in Turtle.
         */
        public static final String MEDIA_TYPE = "text/turtle";

        /**
         * Turtle file extension.
         */
        public static final String FILE_EXTENSION = ".ttl";

        private Turtle() {
            throw new AssertionError();
        }
    }


    /**
     * Useful HTTP request query parameters used by the application REST API.
     */
    public static final class QueryParams {

        /**
         * HTTP request query parameter denoting identifier namespace.
         *
         * <p>Used in connection with normalized name of an individual.
         */
        public static final String NAMESPACE = "namespace";

        /**
         * HTTP request query parameter denoting page number.
         *
         * <p>Used for paging in collections of results.
         *
         * @see #PAGE_SIZE
         */
        public static final String PAGE = "page";

        /**
         * HTTP request query parameter denoting page size.
         *
         * <p>Used for paging in collections of results.
         *
         * @see #PAGE
         */
        public static final String PAGE_SIZE = "size";

        private QueryParams() {
            throw new AssertionError();
        }
    }
}
