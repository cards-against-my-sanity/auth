package dev.jacobandersen.cams.auth.security.token;

import com.nimbusds.jwt.JWTClaimsSet;
import dev.jacobandersen.cams.auth.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

import java.text.ParseException;

public final class PasswordResetTokenValidationContext extends TokenValidationContext {
    public static final PasswordResetTokenValidationContext NULL = new PasswordResetTokenValidationContext(null);
    private static final Logger logger = LoggerFactory.getLogger(PasswordResetTokenValidationContext.class);

    public PasswordResetTokenValidationContext(JWTClaimsSet claimsSet) {
        super(claimsSet);
    }

    @Override
    protected boolean isValidFor(@NonNull User user, @NonNull JWTClaimsSet claimsSet) {
        try {
            return user.getPassword().equals(claimsSet.getStringClaim("vfh"));
        } catch (ParseException ex) {
            logger.error("Password reset token VFH claim was not parseable", ex);
            return false;
        }
    }
}
