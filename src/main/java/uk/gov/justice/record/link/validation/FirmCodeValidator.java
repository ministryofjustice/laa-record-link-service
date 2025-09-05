package uk.gov.justice.record.link.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.AllArgsConstructor;
import uk.gov.justice.record.link.respository.CcmsUserRepository;

@AllArgsConstructor
public class FirmCodeValidator implements ConstraintValidator<ValidFirmCode, String> {
    private CcmsUserRepository ccmsUserRepository;

    @Override
    public boolean isValid(String firmCode, ConstraintValidatorContext context) {
        return ccmsUserRepository.existsByFirmCode(firmCode);
    }
}