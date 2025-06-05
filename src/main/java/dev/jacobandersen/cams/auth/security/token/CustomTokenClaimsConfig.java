package dev.jacobandersen.cams.auth.security.token;

import dev.jacobandersen.cams.auth.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

@Configuration
public class CustomTokenClaimsConfig {
    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer(UserService userService) {
        return context -> {
            final var tokenType = context.getTokenType();
            if (OAuth2TokenType.ACCESS_TOKEN.equals(tokenType) || OidcParameterNames.ID_TOKEN.equals(tokenType.getValue())) {
                userService.loadOidcUserInfoByEmail(context.getPrincipal().getName())
                        .ifPresent(oidcUserInfo -> context.getClaims()
                                .claims(claims -> claims.putAll(oidcUserInfo.getClaims())));
            }
        };
    }
}
