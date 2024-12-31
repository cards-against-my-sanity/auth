package dev.jacobandersen.cams.auth.service;

import dev.jacobandersen.cams.auth.email.CamsEmail;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public <T extends CamsEmail> void sendMail(T message) throws MessagingException {
        message.send(mailSender);
    }
}
