package uk.gov.justice.record.link.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Roles {
    INTERNAL("REQUESTS TO TRANSFER CCMS CASES_VIEWER_INTERN"),
    EXTERNAL("REQUESTS TO TRANSFER CCMS CASES_VIEWER_EXTERN");

    private final String roleName;
}