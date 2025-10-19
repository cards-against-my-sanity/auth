package dev.jacobandersen.cams.auth.email;

import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.template.PebbleTemplate;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

public abstract class BaseEmail {
    private final String templateName;

    protected BaseEmail(String templateName) {
        this.templateName = templateName;
    }

    public abstract String getRecipient();

    public abstract String getSubject();

    public abstract void fillContext(Map<String, Object> context);

    public final void send(final JavaMailSender mailSender, final PebbleEngine engine, final Map<String, Object> context) throws MessagingException {
        final MimeMessage message = mailSender.createMimeMessage();
        final MimeMessageHelper helper = new MimeMessageHelper(message, StandardCharsets.UTF_8.name());

        helper.setTo(getRecipient());
        helper.setSubject(getSubject());

        fillContext(context);

        final String html;
        try {
            final PebbleTemplate template =  engine.getTemplate(templateName);
            StringWriter htmlWriter = new StringWriter();
            template.evaluate(htmlWriter, context, Locale.ROOT);
            html = htmlWriter.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to render email template %s".formatted(templateName), ex);
        }

        helper.setText(html, true);

        mailSender.send(message);
    }
}
