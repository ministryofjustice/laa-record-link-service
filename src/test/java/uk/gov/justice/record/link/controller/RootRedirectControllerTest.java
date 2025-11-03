package uk.gov.justice.record.link.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.justice.record.link.config.DevSecurityConfig;
import uk.gov.justice.record.link.constants.SilasConstants;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RootRedirectController.class)
@Import(DevSecurityConfig.class)
@ActiveProfiles("local")
class RootRedirectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRedirectToExternalPageForExternalUser() throws Exception {
        mockMvc.perform(get("/")
                        .with(oidcLogin()
                                .idToken(token -> token.claims(claims -> {
                                    claims.put(SilasConstants.APP_ROLES, List.of("external"));
                                    claims.put(SilasConstants.SILAS_LOGIN_ID, "externalUser");
                                }))
                        ))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/external/"));
    }

    @Test
    void shouldRedirectToInternalPageForInternalUser() throws Exception {
        mockMvc.perform(get("/")
                        .with(oidcLogin()
                                .idToken(token -> token.claims(claims -> {
                                    claims.put(SilasConstants.APP_ROLES, List.of("internal"));
                                    claims.put(SilasConstants.SILAS_LOGIN_ID, "internalUser");
                                }))
                        ))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/internal/manage-linking-account"));
    }

    @Test
    void shouldRedirectToInternalViewerPageForViewerUser() throws Exception {
        mockMvc.perform(get("/")
                        .with(oidcLogin()
                                .idToken(token -> token.claims(claims -> {
                                    claims.put(SilasConstants.APP_ROLES, List.of("viewer"));
                                    claims.put(SilasConstants.SILAS_LOGIN_ID, "viewerUser");
                                }))
                        ))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/internal/viewer"));
    }

    @Test
    void shouldRedirectToUnauthorizedPageWhenRoleIsMissing() throws Exception {
        mockMvc.perform(get("/")
                        .with(oidcLogin()
                                .idToken(token -> token.claims(claims -> {
                                    // No app_roles claim
                                    claims.put(SilasConstants.SILAS_LOGIN_ID, "noRoleUser");
                                }))
                        ))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/unauthorized"));
    }

    @Test
    void shouldRedirectToUnauthorizedPageForUnknownRole() throws Exception {
        mockMvc.perform(get("/")
                        .with(oidcLogin()
                                .idToken(token -> token.claims(claims -> {
                                    claims.put(SilasConstants.APP_ROLES, List.of("unknown"));
                                    claims.put(SilasConstants.SILAS_LOGIN_ID, "unknownUser");
                                }))
                        ))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/unauthorized"));
    }
}
