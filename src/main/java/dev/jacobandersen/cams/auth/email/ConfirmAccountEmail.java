package dev.jacobandersen.cams.auth.email;

import dev.jacobandersen.cams.auth.model.domain.User;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;

public class ConfirmAccountEmail extends BaseEmail {
    private final User user;
    private final String token;

    public ConfirmAccountEmail(User user, String token) {
        super("confirm-account");
        this.user = user;
        this.token = token;
    }

    @Override
    public String getRecipient() {
        return user.getEmail();
    }

    @Override
    public String getSubject() {
        return "Confirm your InsanityID Account";
    }

    @Override
    public IContext fillContext(Context context) {
        context.setVariable("nickname", user.getNickname());
        context.setVariable("token", token);
        return context;
    }
}
