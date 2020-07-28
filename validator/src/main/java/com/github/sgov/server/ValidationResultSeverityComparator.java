package com.github.sgov.server;

import java.util.Comparator;
import lombok.extern.slf4j.Slf4j;
import org.topbraid.shacl.validation.ValidationResult;

@Slf4j
public class ValidationResultSeverityComparator implements Comparator<ValidationResult> {

    class Shacl {
        private static final String VIOLATION = "http://www.w3.org/ns/shacl#Violation";
        private static final String WARNING = "http://www.w3.org/ns/shacl#Warning";
        private static final String INFO = "http://www.w3.org/ns/shacl#Info";
    }

    /**
     * Compares results based on the severity.
     *
     * @param res1 first validation result
     * @param res2 second validation result
     * @return -1,0,1 as per comparison contract
     */
    public int compare(ValidationResult res1, ValidationResult res2) {
        if (res1.getSeverity().getURI().equals(res2.getSeverity().getURI())) {
            return 0;
        }
        if (res1.getSeverity().getURI().equals(Shacl.VIOLATION)) {
            return -1;
        }
        if (res2.getSeverity().getURI().equals(Shacl.VIOLATION)) {
            return 1;
        }
        if (res1.getSeverity().getURI().equals(Shacl.WARNING)) {
            return -1;
        }
        if (res2.getSeverity().getURI().equals(Shacl.WARNING)) {
            return 1;
        }
        if (res1.getSeverity().getURI().equals(Shacl.INFO)) {
            return -1;
        }
        return 1;
    }
}
