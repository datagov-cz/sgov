package com.github.sgov.server.service;

import com.github.sgov.server.config.conf.UserConf;
import java.net.URI;
import java.text.Normalizer;
import java.util.Objects;
import org.springframework.stereotype.Service;

/**
 * Service for generating and resolving identifiers.
 */
@Service
@SuppressWarnings("checkstyle:MultipleStringLiterals")
public class IdentifierResolver {

    private static final char REPLACEMENT_CHARACTER = '-';

    /**
     * Normalizes the specified value. This includes:
     * <ul>
     * <li>Transforming the value to lower case</li>
     * <li>Trimming the string</li>
     * <li>Replacing non-ASCII characters with ASCII, e.g., 'č' with 'c'</li>
     * <li>Replacing white spaces and slashes with dashes</li>
     * <li>Removing parentheses</li>
     * </ul>
     *
     * <p>Based on
     * <a href="https://gist.github.com/rponte/893494">https://gist.github.com/rponte/893494</a>
     *
     * @param value The value to normalize
     * @return Normalized string
     */
    static String normalize(String value) {
        Objects.requireNonNull(value);
        final String normalized = value.toLowerCase().trim()
            .replaceAll("[\\s/\\\\]", Character.toString(REPLACEMENT_CHARACTER));
        return Normalizer.normalize(normalized, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "")
            .replaceAll("[(?&)]", "");
    }

    /**
     * Extracts locally unique identifier fragment from the specified URI.
     *
     * @param uri URI to extract fragment from
     * @return Identification fragment
     */
    public static String extractIdentifierFragment(URI uri) {
        Objects.requireNonNull(uri);
        final String strUri = uri.toString();
        final int slashIndex = strUri.lastIndexOf("/");
        final int hashIndex = strUri.lastIndexOf("#");
        return strUri.substring((Math.max(slashIndex, hashIndex)) + 1);
    }

    /**
     * Extracts namespace from the specified URI.
     *
     * <p>Namespace in this case means the part of the URI up to the last forward slash or hash-tag,
     * whichever comes later.
     *
     * @param uri URI to extract namespace from
     * @return Identifier namespace
     */
    public static String extractIdentifierNamespace(URI uri) {
        final String strUri = uri.toString();
        final int slashIndex = strUri.lastIndexOf("/");
        final int hashIndex = strUri.lastIndexOf("#");
        return strUri.substring(0, (Math.max(slashIndex, hashIndex)) + 1);
    }

    @SuppressWarnings("checkstyle:ReturnCount")
    private static boolean isUri(String value) {
        try {
            if (!value.matches("^(https?|ftp|file)://.+")) {
                return false;
            }
            URI.create(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Generates user identifier, appending a normalized string consisting of the specified
     * components to the namespace.
     *
     * @param components Components to normalize and add to the identifier
     * @return Generated identifier
     */
    public static URI generateUserIdentifier(String... components) {
        return generateIdentifier(UserConf.namespace, components);
    }

    private static URI generateIdentifier(String ns, String... components) {
        Objects.requireNonNull(ns);
        String namespace = ns;
        if (components.length == 0) {
            throw new IllegalArgumentException("Must provide at least one component for identifier "
                + "generation.");
        }
        final String comps = String.join("-", components);
        if (isUri(comps)) {
            return URI.create(comps);
        }
        if (!namespace.endsWith("/") && !namespace.endsWith("#")) {
            namespace += "/";
        }
        return URI.create(namespace + normalize(comps));
    }

    /**
     * Builds an identifier from the specified namespace and fragment.
     *
     * <p>This method assumes that the fragment is a normalized string uniquely identifying a
     * resource in the specified namespace.
     *
     * <p>Basically, the returned identifier should be the same as would be returned for
     * non-normalized fragments
     *
     * @param namespace Identifier namespace
     * @param fragment  Normalized string unique in the specified namespace
     * @return Identifier
     */
    public static URI resolveIdentifier(String namespace, String fragment) {
        Objects.requireNonNull(namespace);
        String ns = namespace;
        if (!ns.endsWith("/") && !ns.endsWith("#")) {
            ns += "/";
        }
        return URI.create(ns + fragment);
    }
}
