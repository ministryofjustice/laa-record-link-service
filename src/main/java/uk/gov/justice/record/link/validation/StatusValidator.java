package uk.gov.justice.record.link.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.justice.record.link.entity.Status;
import uk.gov.justice.record.link.respository.LinkedRequestRepository;

import java.util.List;

@Component
@AllArgsConstructor
public class StatusValidator implements ConstraintValidator<ValidStatus, String> {

    private final LinkedRequestRepository linkedRequestRepository;

    @Override
    public void initialize(ValidStatus linkedRequestConstraint) {
    }

    @Override
    public boolean isValid(String oldLogin, ConstraintValidatorContext constraintValidatorContext) {
        return isOfValidStatus(oldLogin);
    }

    private Boolean isOfValidStatus(final String oldLogin) {
        final int count = linkedRequestRepository
                .countByCcmsUser_LoginIdAndStatusIn(oldLogin, List.of(uk.gov.justice.record.link.entity.Status.OPEN, uk.gov.justice.record.link.entity.Status.APPROVED));
        final int requestAlreadyOpened = linkedRequestRepository.countByOldLoginIdAndStatus(oldLogin, Status.OPEN);
        return count >= 1 || requestAlreadyOpened >= 1 ? Boolean.FALSE : Boolean.TRUE;
    }
}
