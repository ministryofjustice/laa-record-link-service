package uk.gov.justice.record.link.model;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.justice.record.link.validation.ValidClosedAccount;
import uk.gov.justice.record.link.validation.ValidLoginId;
import uk.gov.justice.record.link.validation.ValidStatus;
import uk.gov.justice.record.link.validation.groups.OnCreateRequest;
import uk.gov.justice.record.link.validation.groups.OnSubmitRequestClosedAccount;
import uk.gov.justice.record.link.validation.groups.OnSubmitRequestLoginId;
import uk.gov.justice.record.link.validation.groups.OnSubmitRequestStatus;

@Getter
@Setter
@NoArgsConstructor
public class UserTransferRequest {

    @ValidLoginId(groups = OnSubmitRequestLoginId.class)
    @ValidClosedAccount(groups = OnSubmitRequestClosedAccount.class)
    @ValidStatus(groups = OnSubmitRequestStatus.class)
    @NotEmpty(message = "Enter CCMS username", groups = OnCreateRequest.class)
    private String oldLogin;
    private String additionalInfo;
}