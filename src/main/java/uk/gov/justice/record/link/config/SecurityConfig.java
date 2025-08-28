package uk.gov.justice.record.link.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import uk.gov.justice.record.link.constants.Roles;
import uk.gov.justice.record.link.constants.SilasConstants;

import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
@Profile("!local")
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**", "/health/**").permitAll()
                        .anyRequest().authenticated())
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.oidcUserService(new OidcUserService()))
                        .successHandler(customSuccessHandler())
                        .failureUrl("/login?error=true"))
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.deny()));
        return http.build();
    }

    private List<String> extractList(Object raw) {
        if (raw instanceof List<?> rawList) {
            return rawList.stream().map(Object::toString).toList();
        } else if (raw instanceof String str) {
            return List.of(str.split(","));
        } else {
            return List.of();
        }
    }

    AuthenticationSuccessHandler customSuccessHandler() {
        return (HttpServletRequest request, HttpServletResponse response, Authentication authentication) -> {

            Object principal = authentication.getPrincipal();

            if (principal instanceof OidcUser oidcUser) {
                Map<String, Object> attributes = oidcUser.getAttributes();

                Object appRolesRaw = attributes.get(SilasConstants.APP_ROLES);
                List<String> roles = extractList(appRolesRaw != null ? appRolesRaw : "");

                String redirectUrl;

                if (roles.stream().anyMatch(r -> r.equalsIgnoreCase(Roles.INTERNAL.getRoleName()))) {
                    redirectUrl = "/manage-linking-account";
                } else if (roles.stream().anyMatch(r -> r.equalsIgnoreCase(Roles.EXTERNAL.getRoleName()))) {
                    redirectUrl = "/";
                } else {
                    redirectUrl = "/not-authorised";
                }
                response.sendRedirect(redirectUrl);
            } else {
                response.sendRedirect("/not-authorised");
            }
        };
    }
}
