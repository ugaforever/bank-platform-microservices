package ru.ugaforever.bank.frontui.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.config.Customizer;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   ClientRegistrationRepository clientRegistrationRepository) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/images/**", "/css/**", "/js/**", "/favicon.ico").permitAll()
                        .requestMatchers("/admin").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/oauth2/authorization/keycloak")
                        .defaultSuccessUrl("/account", true)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(oidcLogoutSuccessHandler(clientRegistrationRepository))
                )
                .oauth2Client(Customizer.withDefaults());

        return http.build();
    }

    private LogoutSuccessHandler oidcLogoutSuccessHandler(
            ClientRegistrationRepository clientRegistrationRepository) {

        OidcClientInitiatedLogoutSuccessHandler successHandler =
                new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);

        successHandler.setPostLogoutRedirectUri("{baseUrl}");

        return successHandler;
    }
}
