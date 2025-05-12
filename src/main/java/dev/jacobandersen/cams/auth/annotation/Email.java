package dev.jacobandersen.cams.auth.annotation;

import dev.jacobandersen.cams.auth.validator.EmailValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EmailValidator.class)
@Documented
public @interface Email {
    String message() default "Email is not valid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
