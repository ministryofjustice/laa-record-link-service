package uk.gov.justice.record.link.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import uk.gov.justice.record.link.constants.SilasConstants;

import java.util.List;

@RequiredArgsConstructor
public class OidcTokenClaimsExtractor {

    private final OidcUser oidcUser;

    public String getUserName() {
        return getClaimAsString(SilasConstants.SILAS_LOGIN_ID);
    }

    public String getEmail() {
        return getClaimAsString(SilasConstants.USER_EMAIL);
    }

    public String getFirstName() {
        return getClaimAsString(SilasConstants.FIRST_NAME);
    }

    public String getLastName() {
        return getClaimAsString(SilasConstants.SURNAME);
    }

    public String getFirmName() {
        return getClaimAsString(SilasConstants.FIRM_NAME);
    }

    public String getFirmCode() {
        return getClaimAsString(SilasConstants.FIRM_CODE);
    }

    public List<String> getRoles() {
        Object roles = oidcUser.getClaims().get(SilasConstants.APP_ROLES);
        if (roles instanceof List<?>) {
            return ((List<?>) roles).stream().map(Object::toString).toList();
        } else if (roles instanceof String) {
            return List.of(((String) roles).split(","));
        }
        return List.of();
    }

    private String getClaimAsString(String claimKey) {
        Object claim = oidcUser.getClaims().get(claimKey);
        return claim != null ? claim.toString() : null;
    }
}
