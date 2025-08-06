package uk.gov.justice.record.link.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.AllArgsConstructor;
import uk.gov.justice.record.link.respository.CcmsUserRepository;

@AllArgsConstructor
public final class LoginIdValidator implements ConstraintValidator<ValidLoginId, String>  {
    private final CcmsUserRepository ccmsUserRepository;

    @Override
    public boolean isValid(final String oldLogin, final ConstraintValidatorContext context) {
        return ccmsUserRepository.findByLoginId(oldLogin).isPresent();
    }
}
