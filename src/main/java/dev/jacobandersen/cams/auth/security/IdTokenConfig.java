package dev.jacobandersen.cams.auth.security;

import dev.jacobandersen.cams.auth.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

@Configuration
public class IdTokenConfig {
    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer(UserService userService) {
        return context -> {
            if (OidcParameterNames.ID_TOKEN.equals(context.getTokenType().getValue())) {
                userService.loadOidcUserInfoByEmail(context.getPrincipal().getName())
                        .ifPresent(oidcUserInfo -> context.getClaims()
                                .claims(claims -> claims.putAll(oidcUserInfo.getClaims())));
            }
        };
    }
}
