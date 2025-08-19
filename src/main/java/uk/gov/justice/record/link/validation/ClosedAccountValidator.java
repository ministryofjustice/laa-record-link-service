package uk.gov.justice.record.link.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ClosedAccountValidator implements ConstraintValidator<ValidClosedAccount, String> {

    @Override
    public boolean isValid(final String oldLogin, final ConstraintValidatorContext context) {
        return !oldLogin.equalsIgnoreCase("ONLINE-SUPPORT@LEGALAID.GSI.GOV.UK");
    }
}
