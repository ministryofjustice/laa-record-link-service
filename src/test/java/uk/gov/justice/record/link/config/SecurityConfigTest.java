package uk.gov.justice.record.link.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Import({SecurityConfig.class})
@ExtendWith(MockitoExtension.class)
public class SecurityConfigTest {

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @Mock
    Authentication authentication;

    @Mock
    OidcUser oidcUser;

    @InjectMocks
    SecurityConfig securityConfig;

    @Test
    void shouldRedirectToInternalPage() throws Exception {
        Map<String, Object> attributes = Map.of("LAA_APP_ROLES", List.of("CCMS case transfer requests - Internal"));

        when(authentication.getPrincipal()).thenReturn(oidcUser);
        when(oidcUser.getAttributes()).thenReturn(attributes);

        AuthenticationSuccessHandler handler = securityConfig.customSuccessHandler();
        handler.onAuthenticationSuccess(request, response, authentication);

        verify(response).sendRedirect("/internal/manage-linking-account");
    }

    @Test
    void shouldRedirectToExternalPage() throws Exception {
        Map<String, Object> attributes = Map.of("LAA_APP_ROLES", List.of("CCMS case transfer requests - External"));

        when(authentication.getPrincipal()).thenReturn(oidcUser);
        when(oidcUser.getAttributes()).thenReturn(attributes);

        AuthenticationSuccessHandler handler = securityConfig.customSuccessHandler();
        handler.onAuthenticationSuccess(request, response, authentication);

        verify(response).sendRedirect("/external/");
    }

    @Test
    void shouldRedirectToUnauthorisedPage() throws Exception {
        Map<String, Object> attributes = Map.of("LAA_APP_ROLES", List.of("UNAUTHORIZED"));

        when(authentication.getPrincipal()).thenReturn(oidcUser);
        when(oidcUser.getAttributes()).thenReturn(attributes);

        AuthenticationSuccessHandler handler = securityConfig.customSuccessHandler();
        handler.onAuthenticationSuccess(request, response, authentication);

        verify(response).sendRedirect("/not-authorised");
    }


}
