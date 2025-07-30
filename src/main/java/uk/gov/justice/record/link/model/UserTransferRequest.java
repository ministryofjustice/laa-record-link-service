package uk.gov.justice.record.link.model;

public record UserTransferRequest(String oldLogin, String additionalInfo) {
    public UserTransferRequest() {
        this("", "");
    }
}