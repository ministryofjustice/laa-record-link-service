package uk.gov.justice.record.link.validation.groups;

import jakarta.validation.GroupSequence;


@GroupSequence({ OnSubmitRequestLoginId.class,  OnSubmitRequestClosedAccount.class, OnSubmitRequestFirmCode.class, OnSubmitRequestStatus.class})
public interface SubmissionValidationSequence {
}
