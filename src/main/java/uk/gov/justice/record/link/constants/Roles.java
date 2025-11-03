package uk.gov.justice.record.link.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Roles {
    INTERNAL("CCMS case transfer requests - Internal"),
    INTERNAL_VIEWER("CCMS case transfer requests - Viewer"),
    EXTERNAL("CCMS case transfer requests - External");

    private final String roleName;
}