package dev.jacobandersen.cams.auth.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.token.Sha512DigestUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Configuration
public class EncoderConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        final PasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder(12);
        final PasswordEncoder bcryptSha512PasswordEncoder = new PasswordEncoder() {
            private String sha(final CharSequence rawPassword) {
                return Sha512DigestUtils.shaHex(rawPassword.toString()).toLowerCase(Locale.ROOT);
            }

            @Override
            public String encode(CharSequence rawPassword) {
                return bCryptPasswordEncoder.encode(sha(rawPassword));
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return bCryptPasswordEncoder.matches(
                        sha(rawPassword),
                        encodedPassword
                );
            }

            @Override
            public boolean upgradeEncoding(String encodedPassword) {
                return bCryptPasswordEncoder.upgradeEncoding(encodedPassword);
            }
        };

        final String customEncoderId = "bcrypt(sha512)";
        final Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put("bcrypt", bCryptPasswordEncoder);
        encoders.put(customEncoderId, bcryptSha512PasswordEncoder);

        return new DelegatingPasswordEncoder(customEncoderId, encoders);
    }
}
