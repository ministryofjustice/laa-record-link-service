package uk.gov.justice.record.link.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import uk.gov.justice.record.link.constants.ValidationConstants;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target({ElementType.TYPE})
@Constraint(validatedBy = UserTransferValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ValidUserTransfer {
    String message() default ValidationConstants.ACCOUNT_ALREADY_ASSIGNED;

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
