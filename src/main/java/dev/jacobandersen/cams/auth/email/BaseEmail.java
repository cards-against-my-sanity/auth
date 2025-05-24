package dev.jacobandersen.cams.auth.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;

import java.nio.charset.StandardCharsets;

public abstract class BaseEmail {
    private final String template;

    protected BaseEmail(String template) {
        this.template = template;
    }

    public abstract String getRecipient();

    public abstract String getSubject();

    public abstract IContext fillContext(Context context);

    public final void send(final JavaMailSender mailSender, final TemplateEngine templateEngine, final Context baseContext) throws MessagingException {
        final MimeMessage message = mailSender.createMimeMessage();
        final MimeMessageHelper helper = new MimeMessageHelper(message, StandardCharsets.UTF_8.name());

        helper.setTo(getRecipient());
        helper.setSubject(getSubject());

        final IContext context = fillContext(baseContext);
        final String html = templateEngine.process(template, context);
        helper.setText(html, true);

        mailSender.send(message);
    }
}
