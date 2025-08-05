package uk.gov.justice.record.link.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD})
@Constraint(validatedBy = InvalidStatusValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface InvalidStatus {
    String message() default "Login processed";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
