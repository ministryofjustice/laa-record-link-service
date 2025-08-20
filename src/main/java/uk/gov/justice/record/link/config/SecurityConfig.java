package uk.gov.justice.record.link.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(this.oidcUserService())
                        )
                );
        return http.build();
    }

    private OidcUserService oidcUserService() {
        return new OidcUserService() {
            @Override
            public OidcUser loadUser(OidcUserRequest userRequest) {
                OidcUser oidcUser = super.loadUser(userRequest);

                // 🔍 Access claims from ID token
                String email = oidcUser.getEmail();
                String name = oidcUser.getFullName();
                Object customClaim = oidcUser.getClaims();

                // You can log or store these claims as needed
                System.out.println("Email: " + email);
                System.out.println("Name: " + name);
                System.out.println("Custom Claim: " + customClaim);

                return oidcUser;
            }
        };
    }
}
