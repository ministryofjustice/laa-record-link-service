package uk.gov.justice.record.link.dto;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class CcmsUserDto {
    private String loginId;
    private String firstName;
    private String lastName;
    private String firmName;
    private String firmCode;
    private String email;
}