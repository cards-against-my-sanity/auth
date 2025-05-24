package dev.jacobandersen.cams.auth.validator;

import dev.jacobandersen.cams.auth.api.HasConfirmablePassword;
import dev.jacobandersen.cams.auth.api.annotation.PasswordMatches;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, HasConfirmablePassword> {
    @Override
    public boolean isValid(HasConfirmablePassword value, ConstraintValidatorContext context) {
        if (value.getPassword().equals(value.getPasswordConfirmation())) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode("passwordConfirmation")
                .addConstraintViolation();

        return false;
    }
}
