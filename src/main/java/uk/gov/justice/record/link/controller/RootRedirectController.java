package uk.gov.justice.record.link.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.justice.record.link.service.OidcTokenClaimsExtractor;

import java.util.List;

import static uk.gov.justice.record.link.util.UserRoleIdentifier.isExternalUser;
import static uk.gov.justice.record.link.util.UserRoleIdentifier.isInternalAdmin;
import static uk.gov.justice.record.link.util.UserRoleIdentifier.isInternalViewer;

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

            boolean isInternal = isInternalAdmin(roles);
            log.info("Internal Roles: {}", isInternal);

            boolean isInternalViewer = isInternalViewer(roles);
            log.info("Internal viewer role: {}", isInternalViewer);

            boolean isExternal = isExternalUser(roles);
            log.info("External Roles: {}", isExternal);

            if (isInternal) {
                return "redirect:/internal/manage-linking-account";
            } else if (isExternal) {
                return "redirect:/external/";
            } else if (isInternalViewer) {
                return "redirect:/internal/viewer";
            }

        }
        return "redirect:/unauthorized";
    }
}