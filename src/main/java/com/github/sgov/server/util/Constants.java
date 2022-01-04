package com.github.sgov.server.util;

/**
 * Application-wide constants.
 */
public class Constants {

    public static final String SERIALIZATION_LANGUAGE = "cs";

    public static final String CC_BY_SA_4 = "https://creativecommons.org/licenses/by-sa/4.0";
    public static final String GITHUB_API_REPOS = "https://api.github.com/repos/";

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
         * HTTP request query parameter denoting vocabulary IRI
         *
         * <p>URI of the vocabulary.
         */
        public static final String VOCABULARY_IRI = "vocabularyIri";

        private QueryParams() {
            throw new AssertionError();
        }
    }
}
