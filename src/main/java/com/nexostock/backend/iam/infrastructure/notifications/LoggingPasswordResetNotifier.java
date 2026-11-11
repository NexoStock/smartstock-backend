package com.nexostock.backend.iam.infrastructure.notifications;

import com.nexostock.backend.iam.application.internal.outboundservices.notifications.PasswordResetNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Placeholder sender: replace with the SMTP sender when external-services is merged. */
@Service
public class LoggingPasswordResetNotifier implements PasswordResetNotifier {

    private static final Logger log = LoggerFactory.getLogger(LoggingPasswordResetNotifier.class);

    private final boolean logToken;

    public LoggingPasswordResetNotifier(@Value("${app.password-reset.log-token:false}") boolean logToken) {
        this.logToken = logToken;
    }

    @Override
    public void sendResetLink(String email, String rawToken) {
        if (logToken) {
            log.info("Password reset for {} -> token: {}", email, rawToken);
        } else {
            log.info("Password reset requested for {}", email);
        }
    }
}