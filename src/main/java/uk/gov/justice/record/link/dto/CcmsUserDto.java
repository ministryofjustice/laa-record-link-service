package uk.gov.justice.record.link.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CcmsUserDto {
    private String loginId;
    private String firstName;
    private String lastName;
    private String firmName;
    private String firmCode;
    private String email;
}