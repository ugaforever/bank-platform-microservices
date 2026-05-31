package ru.ugaforever.bank.chassis.config;

import feign.Client;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.httpclient.ApacheHttpClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

@Slf4j
@Configuration
public class FeignConfig {

    // Для PATCH методов
    @Bean
    public Client feignClient() {
        return new ApacheHttpClient();
    }

    @Bean
    public RequestInterceptor bearerTokenRequestInterceptor(
            OAuth2AuthorizedClientService authorizedClientService,
            OAuth2AuthorizedClientManager authorizedClientManager) {

        return requestTemplate -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
                String registrationId = oauthToken.getAuthorizedClientRegistrationId();
                String principalName = oauthToken.getName();

                OAuth2AuthorizedClient authorizedClient = null;
                try {
                    authorizedClient = authorizedClientService.loadAuthorizedClient(registrationId, principalName);

                    OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                            .withClientRegistrationId(registrationId)
                            .principal(authentication)
                            .build();

                    OAuth2AuthorizedClient refreshedClient = authorizedClientManager.authorize(authorizeRequest);
                    if (refreshedClient != null && refreshedClient.getAccessToken() != null) {
                        authorizedClient = refreshedClient;
                    }

                } catch (Exception e) {
                    log.warn("Failed to get authorized client: {}", e.getMessage());
                }

                if (authorizedClient != null && authorizedClient.getAccessToken() != null) {
                    String token = authorizedClient.getAccessToken().getTokenValue();
                    log.debug("Adding Bearer token (expires at: {})", authorizedClient.getAccessToken().getExpiresAt());
                    requestTemplate.header("Authorization", "Bearer " + token);
                } else {
                    log.warn("No valid token found for {}:{}", registrationId, principalName);
                }
            }
        };
    }

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService) {

        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                        clientRegistrationRepository, authorizedClientService);

        authorizedClientManager.setAuthorizedClientProvider(
                new DelegatingOAuth2AuthorizedClientProvider(
                        new RefreshTokenOAuth2AuthorizedClientProvider(),
                        new AuthorizationCodeOAuth2AuthorizedClientProvider()
                )
        );

        return authorizedClientManager;
    }
}