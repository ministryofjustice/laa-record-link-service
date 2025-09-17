package uk.gov.justice.record.link.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.justice.record.link.entity.Status;
import uk.gov.justice.record.link.model.UserTransferRequest;
import uk.gov.justice.record.link.respository.LinkedRequestRepository;

import java.util.List;

@Component
@AllArgsConstructor
public class UserTransferValidator implements ConstraintValidator<ValidUserTransfer, UserTransferRequest> {

    private final LinkedRequestRepository linkedRequestRepository;

    @Override
    public void initialize(ValidUserTransfer constraintAnnotation) {
    }

    @Override
    public boolean isValid(UserTransferRequest request, ConstraintValidatorContext context) {
        return !hasApprovedRequestForSameFirmAndLogin(request);
    }

    private boolean hasApprovedRequestForSameFirmAndLogin(UserTransferRequest request) {
        int approvedCount = linkedRequestRepository.countByOldLoginIdAndIdamFirmCodeAndStatusIn(
                request.getOldLogin(),
                request.getFirmCode(),
                List.of(Status.APPROVED)
        );
        return approvedCount > 0;
    }
}