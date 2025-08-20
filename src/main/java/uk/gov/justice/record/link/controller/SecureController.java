package uk.gov.justice.record.link.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SecureController {

    @GetMapping("/test")
    public String getUserInfo(@AuthenticationPrincipal OidcUser oidcUser) {
        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();
        Object appAccounts = oidcUser.getClaims();

        return "Hello " + name + " (" + email + ") The custom claims " + appAccounts;
    }
}

