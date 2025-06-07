package dev.jacobandersen.cams.auth.security.token;

import com.nimbusds.jose.*;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import dev.jacobandersen.cams.auth.model.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.time.Duration;
import java.util.*;

@Component
public class JwtUtil {
    private final String keyId;
    private final JWSSigner signer;
    private final JWSVerifier verifier;

    @Autowired
    public JwtUtil(@Qualifier("authKeyId") String keyId, JWSSigner signer, JWSVerifier verifier) {
        this.keyId = keyId;
        this.signer = signer;
        this.verifier = verifier;
    }

    private String createToken(UUID userId, Duration validDuration, String purpose, Map<String, Object> additionalClaims) throws JOSEException {
        final Date now = new Date();
        final JWTClaimsSet.Builder claimsSetBuilder = new JWTClaimsSet.Builder()
                .issuer(keyId)
                .audience(keyId)
                .notBeforeTime(now)
                .issueTime(now)
                .expirationTime(new Date(now.getTime() + validDuration.toMillis()))
                .subject(userId.toString())
                .claim("purpose", purpose);

        additionalClaims.forEach(claimsSetBuilder::claim);

        final SignedJWT signedToken = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(keyId).build(),
                claimsSetBuilder.build()
        );

        signedToken.sign(signer);

        return signedToken.serialize();
    }

    public String createConfirmationToken(User user, Duration duration) throws JOSEException {
        return createToken(user.getId(), duration, "confirmation", Collections.emptyMap());
    }

    public String createPasswordResetToken(User user, Duration duration) throws JOSEException {
        return createToken(user.getId(), duration, "password-reset", Collections.singletonMap(
                "vfh", user.getPassword()
        ));
    }

    private JWTClaimsSet validateToken(@NonNull final String token, @NonNull final String expectPurpose) throws ParseException, JOSEException {
        final SignedJWT signedJWT = SignedJWT.parse(token);
        if (!signedJWT.verify(verifier)) {
            return null;
        }

        final JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();

        final String issuer = claimsSet.getIssuer();
        if (null == issuer || !issuer.equals(keyId)) {
            return null;
        }

        final List<String> audience = claimsSet.getAudience();
        if (audience.isEmpty() || !audience.contains(issuer)) {
            return null;
        }

        final Date now = new Date();

        final Date notBeforeTime = claimsSet.getNotBeforeTime();
        if (null == notBeforeTime || notBeforeTime.after(now)) {
            return null;
        }

        final Date expirationTime = claimsSet.getExpirationTime();
        if (null == expirationTime || expirationTime.before(now)) {
            return null;
        }

        final String subject = claimsSet.getSubject();
        if (null == subject) {
            return null;
        }

        final String purpose = claimsSet.getStringClaim("purpose");
        if (null == purpose || !purpose.equals(expectPurpose)) {
            return null;
        }

        return claimsSet;
    }

    public ConfirmationTokenValidationContext validateConfirmationToken(final String token) throws ParseException, JOSEException {
        return new ConfirmationTokenValidationContext(validateToken(token, "confirmation"));
    }

    public PasswordResetTokenValidationContext validatePasswordResetToken(final String token) throws ParseException, JOSEException {
        return new PasswordResetTokenValidationContext(validateToken(token, "password-reset"));
    }
}
