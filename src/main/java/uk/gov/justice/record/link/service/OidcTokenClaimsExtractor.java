package uk.gov.justice.record.link.service;

import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import java.util.List;

public class OidcTokenClaimsExtractor {

    private final OidcUser oidcUser;

    public OidcTokenClaimsExtractor(OidcUser oidcUser) {
        this.oidcUser = oidcUser;
    }

    public String getUserName() {
        return getClaimAsString("USER_NAME");
    }

    public String getEmail() {
        return getClaimAsString("USER_EMAIL");
    }

    public String getFirstName() {
        return getClaimAsString("given_name");
    }

    public String getLastName() {
        return getClaimAsString("family_name");
    }

    public List<String> getRoles() {
        Object roles = oidcUser.getClaims().get("APP_ROLES");
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
