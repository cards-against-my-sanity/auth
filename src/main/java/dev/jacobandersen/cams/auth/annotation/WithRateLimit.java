package dev.jacobandersen.cams.auth.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Applies a rate limit to a particular method.
 * By default, one call is allowed per 600 seconds
 * (10 minutes).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface WithRateLimit {
    String id();

    int allowedCalls() default 1;

    int limitPeriodSeconds() default 600;
}
