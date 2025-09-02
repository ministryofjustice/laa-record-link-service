package uk.gov.justice.record.link.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    public OidcTokenClaimsExtractor getCurrentUserClaims() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof OidcUser oidcUser) {
            return new OidcTokenClaimsExtractor(oidcUser);
        } else {
            throw new IllegalStateException("Authenticated user is not an OidcUser");
        }
    }
}
