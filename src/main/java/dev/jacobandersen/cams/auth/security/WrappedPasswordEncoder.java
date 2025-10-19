package dev.jacobandersen.cams.auth.security;

import org.springframework.security.core.token.Sha512DigestUtils;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public record WrappedPasswordEncoder(PasswordEncoder wrapper) implements PasswordEncoder {
    public static WrappedPasswordEncoder ofExternalWrap(ExternalWrap wrapper) {
        return new WrappedPasswordEncoder(switch (wrapper) {
            case ARGON2 -> Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
            case BCRYPT -> new BCryptPasswordEncoder(12);
        });
    }

    private String sha512(final CharSequence password) {
        return Sha512DigestUtils.shaHex(password.toString());
    }

    @Override
    public String encode(CharSequence rawPassword) {
        return wrapper.encode(sha512(rawPassword));
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return wrapper.matches(sha512(rawPassword), encodedPassword);
    }

    @Override
    public boolean upgradeEncoding(String encodedPassword) {
        return wrapper.upgradeEncoding(encodedPassword);
    }

    public enum ExternalWrap {
        ARGON2,
        BCRYPT
    }
}
