package com.github.sgov.server;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.topbraid.shacl.validation.ValidationResult;

@Slf4j
public class ValidationResultSeverityComparator implements Comparator<ValidationResult> {

    private static final String SHACL = "http://www.w3.org/ns/shacl#";

    @Getter
    enum ShaclSeverity {
        VIOLATION(SHACL + "Violation"),
        WARNING(SHACL + "Warning"),
        INFO(SHACL + "Info");

        private final String uri;

        ShaclSeverity(final String uri) {
            this.uri = uri;
        }

        public static ShaclSeverity of(final ValidationResult result) {
            final Optional<ShaclSeverity> severity
                = Arrays.stream(ShaclSeverity.values()).filter(s ->
                s.uri.equals(result.getSeverity().getURI())
            ).findAny();
            if (severity.isPresent()) {
                return severity.get();
            } else {
                return null;
            }
        }
    }

    /**
     * Compares results based on the severity.
     *
     * @param res1 first validation result
     * @param res2 second validation result
     * @return negative, 0, positive as per comparison contract
     */
    public int compare(ValidationResult res1, ValidationResult res2) {
        return ShaclSeverity.of(res1).compareTo(ShaclSeverity.of(res2));
    }
}
