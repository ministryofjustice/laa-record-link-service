package uk.gov.justice.record.link.validation.groups;

import jakarta.validation.GroupSequence;

@GroupSequence({ OnSubmitRequestLoginId.class, OnSubmitRequestStatus.class})
public interface SubmissionValidationSequence {
}
