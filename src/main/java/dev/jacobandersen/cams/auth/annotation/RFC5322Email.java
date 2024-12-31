package dev.jacobandersen.cams.auth.annotation;

import dev.jacobandersen.cams.auth.validator.RFC5322EmailValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RFC5322EmailValidator.class)
@Documented
public @interface RFC5322Email {
    String message() default "Email is not valid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
