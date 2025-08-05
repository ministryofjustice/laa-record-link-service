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
public class InvalidStatusValidator implements ConstraintValidator<InvalidStatus, String> {

    private final LinkedRequestRepository linkedRequestRepository;

    @Override
    public void initialize(InvalidStatus linkedRequestConstraint) {
    }

    @Override
    public boolean isValid(String oldLogin, ConstraintValidatorContext constraintValidatorContext) {
        return isOfValidStatus(oldLogin);
    }

    private Boolean isOfValidStatus(final String oldLogin) {
        final int count = linkedRequestRepository
                .countByCcmsUser_LoginIdAndStatusIn(oldLogin, List.of(Status.OPEN, Status.APPROVED));
        return count >= 1 ? Boolean.FALSE : Boolean.TRUE;
    }
}
