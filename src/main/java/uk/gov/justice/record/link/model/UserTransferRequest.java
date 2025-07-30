package uk.gov.justice.record.link.model;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserTransferRequest {

    @NotEmpty(message = "Enter CCMS username")
    private String oldLogin;
    private String additionalInfo;
}