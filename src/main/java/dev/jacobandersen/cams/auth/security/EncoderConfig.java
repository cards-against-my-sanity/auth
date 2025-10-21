package dev.jacobandersen.cams.auth.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class EncoderConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        final String customEncoderId = "argon2(sha512)";
        final Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put(customEncoderId, new Argon2Sha512PasswordEncoder());

        return new DelegatingPasswordEncoder(customEncoderId, encoders);
    }
}
