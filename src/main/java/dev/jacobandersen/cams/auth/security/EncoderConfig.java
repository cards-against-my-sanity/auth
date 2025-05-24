package dev.jacobandersen.cams.auth.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.token.Sha512DigestUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class EncoderConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return bCryptPasswordEncoder.encode(Sha512DigestUtils.shaHex(rawPassword.toString()));
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return bCryptPasswordEncoder.matches(
                        Sha512DigestUtils.shaHex(rawPassword.toString()),
                        encodedPassword
                );
            }

            @Override
            public boolean upgradeEncoding(String encodedPassword) {
                return bCryptPasswordEncoder.upgradeEncoding(encodedPassword);
            }
        };
    }
}
