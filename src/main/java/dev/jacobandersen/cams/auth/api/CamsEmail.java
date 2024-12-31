package dev.jacobandersen.cams.auth.api;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

public abstract class CamsEmail {
    public abstract void defineMessage(MimeMessageHelper message) throws MessagingException;

    public final void send(final JavaMailSender mailSender) throws MessagingException {
        final MimeMessage message = mailSender.createMimeMessage();

        final MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
        defineMessage(helper);

        mailSender.send(message);
    }
}
