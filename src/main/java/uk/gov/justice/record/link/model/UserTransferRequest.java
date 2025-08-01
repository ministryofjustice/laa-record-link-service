package uk.gov.justice.record.link.model;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.justice.record.link.validation.LinkedRequestConstraint;

@Getter
@Setter
@NoArgsConstructor
public class UserTransferRequest {

    @LinkedRequestConstraint
    @NotEmpty(message = "Enter CCMS username")
    private String oldLogin;
    private String additionalInfo;
}