package dev.jacobandersen.cams.auth.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
public class KeyConfig {
    private final String keyId;
    private final RSAPublicKey publicKey;
    private final RSAPrivateKey privateKey;

    @Autowired
    public KeyConfig(@Qualifier("authKeyId") String keyId, RSAPublicKey publicKey, RSAPrivateKey privateKey) {
        this.keyId = keyId;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    @Bean
    public RSAKey rsaKey() {
        return new RSAKey.Builder(publicKey).privateKey(privateKey).keyID(keyId).build();
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        return new ImmutableJWKSet<>(new JWKSet(rsaKey()));
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource());
    }

    @Bean
    public JWSSigner jwsSigner() throws JOSEException {
        return new RSASSASigner(rsaKey());
    }

    @Bean
    public JWSVerifier jwsVerifier() throws JOSEException {
        return new RSASSAVerifier(rsaKey());
    }
}
