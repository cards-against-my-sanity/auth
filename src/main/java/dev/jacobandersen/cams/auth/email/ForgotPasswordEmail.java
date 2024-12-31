package dev.jacobandersen.cams.auth.email;

import dev.jacobandersen.cams.auth.model.User;
import jakarta.mail.MessagingException;
import org.springframework.mail.javamail.MimeMessageHelper;

public class ForgotPasswordEmail extends CamsEmail {
    private final User user;
    private final String url;

    public ForgotPasswordEmail(User user, String token) {
        this.user = user;
        url = String.format("http://localho.st:3000/auth/reset_password/%s", token);
    }

    @Override
    public void defineMessage(MimeMessageHelper message) throws MessagingException {
        message.setFrom("jacob@algorithmjunkie.com");
        message.setTo(user.getEmail());
        message.setSubject("Forgotten Password at Cards Against my Sanity");
        message.setText("Hey there " + user.getNickname() + "!<br /><br />" +
                "Someone (hopefully you) has requested a password reset link for Cards Against my Sanity. " +
                "If you have forgotten your password, click the link below to set a new one:<br /><br />" +
                String.format("<a href=\"%s\">%s</a>", url, url) + "<br />" +
                "<br />Please note: This link will expire in five minutes. If you don't use it before then, you will need to request a new one.<br /><br />" +
                "If you did not request this link, please ignore this email - there is nothing you need to do.", true);
    }
}
