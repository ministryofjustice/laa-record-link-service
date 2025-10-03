package uk.gov.justice.record.link.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.AllArgsConstructor;
import uk.gov.justice.record.link.respository.CcmsUserRepository;

@AllArgsConstructor
public class ClosedAccountValidator implements ConstraintValidator<ValidClosedAccount, String> {

    private final CcmsUserRepository ccmsUserRepository;

    @Override
    public boolean isValid(final String oldLogin, final ConstraintValidatorContext context) {
        if (oldLogin == null || oldLogin.trim().isEmpty()) {
            return false;
        }
        if (oldLogin.contains("@")) {
            // checking for gov.uk emails
            return !containsGovUkDomain(oldLogin);
        } else {
            return ccmsUserRepository.findByLoginId(oldLogin)
                    .map(user -> !containsGovUkDomain(user.getEmail()))
                    .orElse(false);
        }
    }
    
    private boolean containsGovUkDomain(final String email) {
        if (email == null || !email.contains("@")) {
            return false;
        }
        String emailDomain = email.substring(email.indexOf("@") + 1);
        return emailDomain.toLowerCase().contains("gov.uk");
    }
}
