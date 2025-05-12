package dev.jacobandersen.cams.auth.validator;

import dev.jacobandersen.cams.auth.annotation.Email;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EmailValidator implements ConstraintValidator<Email, String> {
    private static final org.apache.commons.validator.routines.EmailValidator EMAIL_VALIDATOR = org.apache.commons.validator.routines.EmailValidator.getInstance();

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value != null && !value.isEmpty() && EMAIL_VALIDATOR.isValid(value);
    }
}
