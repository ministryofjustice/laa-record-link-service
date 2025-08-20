package uk.gov.justice.record.link.entity;

public enum ClosedAccount {
    CLOSED_EMAIL("ONLINE-SUPPORT@LEGALAID.GSI.GOV.UK");

    private final String email;

    ClosedAccount(String email) {
        this.email = email;
    }

    public boolean isAccountClosed(String inputEmail) {
        return inputEmail != null && inputEmail.equalsIgnoreCase(email);
    }
}