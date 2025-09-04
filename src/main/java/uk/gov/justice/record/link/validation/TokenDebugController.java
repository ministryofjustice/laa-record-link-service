package uk.gov.justice.record.link.validation;

import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.context.annotation.Profile;

import java.util.Map;

@Profile("dev")
@RestController
public class TokenDebugController {

    @GetMapping("/debug/token")
    public Map<String, Object> debugToken(@AuthenticationPrincipal OidcUser user) {
        if (user == null) {
            return Map.of("error", "No authenticated user found");
        }

        System.out.println("Token claims: " + user.getClaims());
        
        return user.getClaims();
    }
}
