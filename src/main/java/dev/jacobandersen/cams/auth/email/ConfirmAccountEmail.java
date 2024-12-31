package dev.jacobandersen.cams.auth.email;

import dev.jacobandersen.cams.auth.model.User;
import jakarta.mail.MessagingException;
import org.springframework.mail.javamail.MimeMessageHelper;

public class ConfirmAccountEmail extends CamsEmail {
    private final User user;
    private final String url;

    public ConfirmAccountEmail(User user, String token) {
        this.user = user;
        url = String.format("http://localho.st:3000/auth/confirm/%s", token);
    }

    @Override
    public void defineMessage(MimeMessageHelper message) throws MessagingException {
        message.setFrom("jacob@algorithmjunkie.com");
        message.setTo(user.getEmail());
        message.setSubject("Confirm Account at Cards Against my Sanity");
        message.setText("Hey there " + user.getNickname() + "!<br /><br />" +
                "Someone (hopefully you) has created an account at Cards Against my Sanity. " +
                "Before you can log in and play, please confirm your account at the link below:<br /><br />" +
                String.format("<a href=\"%s\">%s</a>", url, url) + "<br />" +
                "<br />Please note: This link will expire in ten minutes. If you don't use it before then, you will need to request a new one.", true);
    }
}
