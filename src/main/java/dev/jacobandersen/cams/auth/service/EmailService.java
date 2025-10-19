package dev.jacobandersen.cams.auth.service;

import dev.jacobandersen.cams.auth.email.BaseEmail;
import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.loader.ClasspathLoader;
import io.pebbletemplates.pebble.loader.Loader;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class EmailService {
    private final JavaMailSender mailSender;
    private final PebbleEngine pebbleEngine;

    @Value("${app.base-url}")
    private String appBaseUrl;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;

        final Loader<?> loader = new ClasspathLoader();
        loader.setPrefix("email/");
        loader.setSuffix(".peb");
        loader.setCharset("UTF-8");

        pebbleEngine = new PebbleEngine.Builder()
                .loader(loader)
                .build();
    }

    @Async
    public <T extends BaseEmail> void sendMail(T message) throws MessagingException {
        Map<String, Object> context = new HashMap<>();
        context.put("app_base_url", appBaseUrl);

        message.send(mailSender, pebbleEngine, context);
    }
}
