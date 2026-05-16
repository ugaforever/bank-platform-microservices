package ru.ugaforever.bank.cash.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.*;
import java.util.stream.Collectors;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain accountsSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/**").permitAll();
                    //auth.requestMatchers("/actuator/**").permitAll();
                     auth.anyRequest().authenticated();
                });

        http.oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
        );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(this::extractRealmRoles);
        return converter;
    }

    private Collection<GrantedAuthority> extractRealmRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null) {
            return Collections.emptyList();
        }

        Object rolesObj = realmAccess.get("roles");

        // Если провайдер вдруг вернёт не коллекцию (например, строку или null),
        // мы не упадём с ClassCastException,
        // а просто считаем, что ролей нет.
        if (!(rolesObj instanceof Collection<?> rawRoles)) {
            return Collections.emptyList();
        }

        // Приводим всё к списку строк
        List<String> roles = rawRoles.stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .toList();

        // Добавляем "ROLE_<имя роли>" для @PreAuthorize("hasRole('...')")
        List<GrantedAuthority> authorities = roles.stream()
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());

        // Дополнительно маппим бизнес-право на отдельный authority
        if (roles.contains("ACCOUNTS_WRITE")) {
            authorities.add(new SimpleGrantedAuthority("accounts.write"));
        }

        return authorities;
    }
}

