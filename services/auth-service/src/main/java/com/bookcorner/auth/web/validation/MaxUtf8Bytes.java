package com.bookcorner.auth.web.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Limits a string by its size in UTF-8 BYTES, not characters. bcrypt silently ignores everything after 72 bytes,
 * and one character can take up to four bytes, so a character limit would not be safe.
 */
@Documented
@Constraint(validatedBy = MaxUtf8BytesValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MaxUtf8Bytes {

    String message() default "must be at most {value} bytes long (UTF-8)";

    int value();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
