package uk.gov.justice.record.link.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.justice.record.link.service.OidcTokenClaimsExtractor;

import java.util.List;

@Controller
public class RootRedirectController {

    private static final Logger log = LoggerFactory.getLogger(RootRedirectController.class);

    @GetMapping("/")
    public String handleRootRedirect(@AuthenticationPrincipal OidcUser oidcUser) {
        if (oidcUser != null) {
            log.info("OIDC Claims: {}", oidcUser.getClaims());

            OidcTokenClaimsExtractor extractor = new OidcTokenClaimsExtractor(oidcUser);
            List<String> roles = extractor.getRoles();

            log.info("Extracted Roles: {}", roles);

            boolean isInternal = roles.stream()
                    .anyMatch(role -> role.toLowerCase().contains("intern"));
            log.info("Internal Roles: {}", isInternal);

            boolean isExternal = roles.stream()
                    .anyMatch(role -> {
                        String r = role.toLowerCase();
                        return r.contains("external") || r.contains("extern");
                    });
            log.info("External Roles: {}", isExternal);

            if (isInternal) {
                return "redirect:/internal/manage-linking-account";
            } else if (isExternal) {
                return "redirect:/external/";
            }

        }
        return "redirect:/unauthorized";
    }
}