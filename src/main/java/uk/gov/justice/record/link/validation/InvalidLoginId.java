package uk.gov.justice.record.link.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import uk.gov.justice.record.link.constants.ValidationConstants;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD})
@Constraint(validatedBy = InvalidLoginIdValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface InvalidLoginId {
    String message() default ValidationConstants.INVALID_LOGIN_ID_MESSAGE;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
