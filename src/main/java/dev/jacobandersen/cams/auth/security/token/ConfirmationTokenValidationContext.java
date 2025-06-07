package dev.jacobandersen.cams.auth.security.token;

import com.nimbusds.jwt.JWTClaimsSet;
import dev.jacobandersen.cams.auth.model.domain.User;
import org.springframework.lang.NonNull;

public final class ConfirmationTokenValidationContext extends TokenValidationContext {
    public static final ConfirmationTokenValidationContext NULL = new ConfirmationTokenValidationContext(null);

    public ConfirmationTokenValidationContext(JWTClaimsSet claimsSet) {
        super(claimsSet);
    }

    @Override
    protected boolean isValidFor(@NonNull User user, @NonNull JWTClaimsSet claimsSet) {
        return true;
    }
}
