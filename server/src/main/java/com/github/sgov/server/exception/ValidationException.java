package com.github.sgov.server.exception;

import com.github.sgov.server.util.ValidationResult;
import java.util.stream.Collectors;

/**
 * Indicates that invalid data have been passed to the application.
 *
 * <p>The exception message should provide information as to what data are invalid and why.
 */
public class ValidationException extends SGoVException {

    private final ValidationResult<?> validationResult;

    public ValidationException(String message) {
        super(message);
        this.validationResult = null;
    }

    public ValidationException(ValidationResult<?> validationResult) {
        assert !validationResult.isValid();
        this.validationResult = validationResult;
    }

    @Override
    public String getMessage() {
        if (validationResult == null) {
            return super.getMessage();
        }
        return String.join("\n",
            validationResult.getViolations().stream()
                .map(cv -> "Value of " + cv.getRootBeanClass().getSimpleName() + "."
                    + cv.getPropertyPath() + " " + cv.getMessage())
                .collect(Collectors.toSet()));
    }
}
