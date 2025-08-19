package uk.gov.justice.record.link.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.AllArgsConstructor;
import uk.gov.justice.record.link.entity.CcmsUser;
import uk.gov.justice.record.link.respository.CcmsUserRepository;

import java.util.Optional;

@AllArgsConstructor
public class ClosedAccountValidator implements ConstraintValidator<ValidClosedAccount, String> {

    private static final String CLOSED_EMAIL = "ONLINE-SUPPORT@LEGALAID.GSI.GOV.UK";
    private final CcmsUserRepository ccmsUserRepository;

    @Override
    public boolean isValid(final String oldLogin, final ConstraintValidatorContext context) {

        Optional<CcmsUser> user = ccmsUserRepository.findByLoginId(oldLogin);
        return user.isPresent() && !user.get().getEmail().equalsIgnoreCase(CLOSED_EMAIL);
    }
}
