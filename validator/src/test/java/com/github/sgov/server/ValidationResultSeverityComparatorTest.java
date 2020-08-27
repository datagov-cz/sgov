package com.github.sgov.server;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.jena.rdf.model.ResourceFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.topbraid.shacl.validation.ResourceValidationResult;
import org.topbraid.shacl.validation.ValidationResult;

public class ValidationResultSeverityComparatorTest {

    private ValidationResult mockWithSeverity(final String severityUri) {
        final ValidationResult result1 = mock(ResourceValidationResult.class);
        when(result1.getSeverity()).thenReturn(ResourceFactory.createResource(
            severityUri));
        return result1;
    }

    private void testEquals(final String severityUri1, final String severityUri2) {
        ValidationResult result1 = mockWithSeverity(
            severityUri1);
        ValidationResult result2 = mockWithSeverity(
            severityUri2);
        Assertions.assertEquals( 0, new ValidationResultSeverityComparator().compare(result1,result2) );
    }

    private void testGreater(final String severityUri1, final String severityUri2) {
        ValidationResult result1 = mockWithSeverity(
            severityUri1);
        ValidationResult result2 = mockWithSeverity(
            severityUri2);
        Assertions.assertTrue( new ValidationResultSeverityComparator().compare(result1,result2) > 0);
    }

    @Test
    public void compareComparesCorrectlySameValue() {
        testEquals(
            ValidationResultSeverityComparator.ShaclSeverity.VIOLATION.getUri(),
            ValidationResultSeverityComparator.ShaclSeverity.VIOLATION.getUri());

        testEquals(
            ValidationResultSeverityComparator.ShaclSeverity.WARNING.getUri(),
            ValidationResultSeverityComparator.ShaclSeverity.WARNING.getUri());

        testEquals(
            ValidationResultSeverityComparator.ShaclSeverity.INFO.getUri(),
            ValidationResultSeverityComparator.ShaclSeverity.INFO.getUri());
    }

    @Test
    public void compareComparesCorrectlyLessThanValue() {
        testGreater(
            ValidationResultSeverityComparator.ShaclSeverity.WARNING.getUri(),
            ValidationResultSeverityComparator.ShaclSeverity.VIOLATION.getUri()
        );

        testGreater(
            ValidationResultSeverityComparator.ShaclSeverity.INFO.getUri(),
            ValidationResultSeverityComparator.ShaclSeverity.WARNING.getUri()
        );

        testGreater(
            ValidationResultSeverityComparator.ShaclSeverity.INFO.getUri(),
            ValidationResultSeverityComparator.ShaclSeverity.VIOLATION.getUri()
        );
    }

    @Test
    public void compareComparesCorrectlyGreaterThanValue() {
        testGreater(
            ValidationResultSeverityComparator.ShaclSeverity.WARNING.getUri(),
            ValidationResultSeverityComparator.ShaclSeverity.VIOLATION.getUri()
        );

        testGreater(
            ValidationResultSeverityComparator.ShaclSeverity.INFO.getUri(),
            ValidationResultSeverityComparator.ShaclSeverity.WARNING.getUri()
        );

        testGreater(
            ValidationResultSeverityComparator.ShaclSeverity.INFO.getUri(),
            ValidationResultSeverityComparator.ShaclSeverity.VIOLATION.getUri()
        );
    }
}
