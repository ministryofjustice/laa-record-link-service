package uk.gov.justice.record.link.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransferUser {

    private String oldLogin;
    private String additionalInfo;
}