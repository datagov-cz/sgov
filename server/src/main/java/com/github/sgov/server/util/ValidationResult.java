package com.github.sgov.server.util;

import java.io.Serializable;
import java.util.Collection;
import javax.validation.ConstraintViolation;

/**
 * Represents a result of validation using a JSR 380 {@link javax.validation.Validator}.
 *
 * <p>The main reason for the existence of this class is that Java generics are not able to cope
 * with the set of constraint violations of some type produced by the validator.
 *
 * @param <T> The validated type
 */
public final class ValidationResult<T> implements Serializable {

    private final Collection<ConstraintViolation<T>> violations;

    private ValidationResult(Collection<ConstraintViolation<T>> violations) {
        this.violations = violations;
    }

    public static <T> ValidationResult<T> of(Collection<ConstraintViolation<T>> violations) {
        return new ValidationResult<>(violations);
    }

    public Collection<ConstraintViolation<T>> getViolations() {
        return violations;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isValid() {
        return violations.isEmpty();
    }
}
