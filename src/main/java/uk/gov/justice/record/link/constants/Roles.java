package uk.gov.justice.record.link.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Roles {
    INTERNAL("CCMS case transfer requests - Internal"),
    EXTERNAL("CCMS case transfer requests - External");

    private final String roleName;
}