package uk.gov.justice.record.link.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.AllArgsConstructor;
import uk.gov.justice.record.link.entity.ClosedAccount;
import uk.gov.justice.record.link.respository.CcmsUserRepository;

@AllArgsConstructor
public class ClosedAccountValidator implements ConstraintValidator<ValidClosedAccount, String> {

    private final CcmsUserRepository ccmsUserRepository;

    @Override
    public boolean isValid(final String oldLogin, final ConstraintValidatorContext context) {

        return ccmsUserRepository.findByLoginId(oldLogin)
                .map(user -> !ClosedAccount.CLOSED_EMAIL.isAccountClosed(user.getEmail()))
                .orElse(false);
    }
}
