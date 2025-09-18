package uk.gov.justice.record.link.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.AllArgsConstructor;
import uk.gov.justice.record.link.model.UserTransferRequest;
import uk.gov.justice.record.link.respository.CcmsUserRepository;

@AllArgsConstructor
public class FirmCodeValidator implements ConstraintValidator<ValidFirmCode, UserTransferRequest> {
    private CcmsUserRepository ccmsUserRepository;

    @Override
    public boolean isValid(final UserTransferRequest userTransferRequest, final ConstraintValidatorContext context) {
        return ccmsUserRepository.existsByLoginIdAndFirmCode(userTransferRequest.getOldLogin(), userTransferRequest.getFirmCode());
    }
}