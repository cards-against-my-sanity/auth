package dev.jacobandersen.cams.auth.security.token;

import com.nimbusds.jwt.JWTClaimsSet;
import dev.jacobandersen.cams.auth.model.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

import java.util.UUID;

public abstract class TokenValidationContext {
    private static final Logger logger = LoggerFactory.getLogger(TokenValidationContext.class);

    private final JWTClaimsSet claimsSet;
    private final UUID userId;

    protected TokenValidationContext(JWTClaimsSet claimsSet) {
        if (null == claimsSet) {
            this.claimsSet = null;
            this.userId = null;
        } else {
            this.claimsSet = claimsSet;

            final String subject = claimsSet.getSubject();
            if (null == subject) {
                userId = null;
            } else {
                UUID parsedUserId = null;
                try {
                    parsedUserId = UUID.fromString(subject);
                } catch (IllegalArgumentException e) {
                    logger.warn("Attempted to construct TokenValidationContext with invalid subject {}", subject);
                }

                this.userId = parsedUserId;
            }
        }
    }

    public final boolean isInitiallyValid() {
        return null != claimsSet && null != userId;
    }

    public final boolean completeValidation(@NonNull final User user) {
        return isInitiallyValid() && userId.equals(user.id()) && isValidFor(user, claimsSet);
    }

    public UUID getUserId() {
        return userId;
    }

    protected abstract boolean isValidFor(@NonNull final User user, @NonNull JWTClaimsSet claimsSet);
}
