package com.nexostock.backend.alerts.infrastructure.notifications;

import com.nexostock.backend.alerts.domain.model.aggregates.OutboundMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Component
@ConditionalOnProperty(
        name = "app.notifications.enabled",
        havingValue = "true"
)
public class SmtpEmailSender implements NotificationSender {

    private final JavaMailSender mail;
    private final String from;

    public SmtpEmailSender(
            JavaMailSender mail,
            @Value("${app.notifications.from}") String from) {
        this.mail = mail;
        this.from = from;
    }

    @Override
    public boolean supports(String channel) {
        return "email".equals(channel);
    }

    @Override
    public void send(OutboundMessage message) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setFrom(from);
        email.setTo(message.getDestination());
        email.setSubject(message.getSubject());
        email.setText(message.getContent());
        mail.send(email);
    }
}
