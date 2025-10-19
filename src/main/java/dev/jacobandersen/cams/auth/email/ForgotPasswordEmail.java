package dev.jacobandersen.cams.auth.email;

import dev.jacobandersen.cams.auth.model.domain.User;

import java.util.Map;

public class ForgotPasswordEmail extends BaseEmail {
    private final User user;
    private final String token;

    public ForgotPasswordEmail(User user, String token) {
        super("forgot-password");
        this.user = user;
        this.token = token;
    }

    @Override
    public String getRecipient() {
        return user.getEmail();
    }

    @Override
    public String getSubject() {
        return "Forgotten InsanityID Password";
    }

    @Override
    public void fillContext(Map<String, Object> context) {
        context.put("nickname", user.getNickname());
        context.put("token", token);
    }
}
