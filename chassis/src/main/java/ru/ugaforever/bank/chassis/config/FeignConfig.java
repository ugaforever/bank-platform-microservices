package ru.ugaforever.bank.chassis.config;

import feign.Client;
import feign.RequestInterceptor;
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
            OAuth2AuthorizedClientManager authorizedClientManager) {

        return requestTemplate -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            log.debug("=== Feign Interceptor for URL: {} ===", requestTemplate.url());

            String token = null;

            // 1. Пробуем получить пользовательский токен
            if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
                token = getUserToken(oauthToken, authorizedClientManager);
                if (token != null) {
                    log.debug("Using USER token for request: {}", requestTemplate.url());
                }
            }

            // 2. Если нет пользовательского токена, определяем целевой сервис из URL
            if (token == null) {
                String targetService = extractTargetService(requestTemplate.url());
                if (targetService != null) {
                    token = getServiceToken(targetService, authorizedClientManager);
                    if (token != null) {
                        log.debug("Using SERVICE token for '{}' request: {}", targetService, requestTemplate.url());
                    }
                }
            }

            if (token != null) {
                requestTemplate.header("Authorization", "Bearer " + token);
            } else {
                log.warn("No token available for request: {}", requestTemplate.url());
            }
        };
    }

    private String getUserToken(OAuth2AuthenticationToken oauthToken,
                                OAuth2AuthorizedClientManager authorizedClientManager) {
        try {
            String registrationId = oauthToken.getAuthorizedClientRegistrationId();

            OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                    .withClientRegistrationId(registrationId)
                    .principal(oauthToken)
                    .build();

            OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);

            if (authorizedClient != null && authorizedClient.getAccessToken() != null) {
                log.debug("User token obtained for registration: {}", registrationId);
                return authorizedClient.getAccessToken().getTokenValue();
            }
        } catch (Exception e) {
            log.warn("Failed to get user token: {}", e.getMessage());
        }
        return null;
    }

    private String getServiceToken(String serviceName,
                                   OAuth2AuthorizedClientManager authorizedClientManager) {
        try {
            OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                    .withClientRegistrationId(serviceName)
                    .principal(serviceName)
                    .build();

            OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);

            if (authorizedClient != null && authorizedClient.getAccessToken() != null) {
                log.debug("Service token obtained for: {}, expires at: {}",
                        serviceName, authorizedClient.getAccessToken().getExpiresAt());
                return authorizedClient.getAccessToken().getTokenValue();
            }
        } catch (Exception e) {
            log.warn("Failed to get service token for {}: {}", serviceName, e.getMessage());
        }
        return null;
    }

    private String extractTargetService(String url) {
        if (url == null || url.isEmpty()) return null;

        try {
            // Определяем целевой сервис из URL
            String path = url;

            // Если URL полный, извлекаем хост
            if (url.startsWith("http")) {
                java.net.URI uri = java.net.URI.create(url);
                String host = uri.getHost();
                if (host != null && host.contains("-service")) {
                    return host;
                }
                path = uri.getPath();
            }

            // Определяем по пути
            if (path != null) {
                if (path.startsWith("/account")) {
                    return "account-service";
                }
                if (path.startsWith("/cash")) {
                    return "cash-service";
                }
                if (path.startsWith("/transfer")) {
                    return "transfer-service";
                }
                if (path.startsWith("/notification")) {
                    return "notification-service";
                }
            }
        } catch (Exception e) {
            log.debug("Failed to extract service from URL: {}", url);
        }

        return null;
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
                        new AuthorizationCodeOAuth2AuthorizedClientProvider(),
                        new ClientCredentialsOAuth2AuthorizedClientProvider()  // Важно для сервисных токенов!
                )
        );

        return authorizedClientManager;
    }
}