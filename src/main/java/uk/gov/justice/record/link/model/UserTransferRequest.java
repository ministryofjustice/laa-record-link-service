package uk.gov.justice.record.link.model;

import lombok.Getter;
import lombok.Setter;


public record UserTransferRequest(String oldLogin, String additionalInfo) {
    public UserTransferRequest() {
        this("", "");
    }
}