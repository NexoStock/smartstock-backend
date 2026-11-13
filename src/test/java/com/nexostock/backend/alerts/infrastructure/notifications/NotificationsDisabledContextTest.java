package com.nexostock.backend.alerts.infrastructure.notifications;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationsDisabledContextTest {

    private final ApplicationContextRunner runner =
            new ApplicationContextRunner()
                    .withUserConfiguration(
                            SmtpEmailSender.class,
                            NotificationDispatcher.class);

    @Test
    void startsWithoutSmtpWhenNotificationsAreDisabled() {
        runner.withPropertyValues("app.notifications.enabled=false")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).doesNotHaveBean(JavaMailSender.class);
                    assertThat(context).doesNotHaveBean(SmtpEmailSender.class);
                    assertThat(context).doesNotHaveBean(NotificationDispatcher.class);
                });
    }

    @Test
    void notificationsAreDisabledByDefault() {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).doesNotHaveBean(JavaMailSender.class);
            assertThat(context).doesNotHaveBean(SmtpEmailSender.class);
            assertThat(context).doesNotHaveBean(NotificationDispatcher.class);
        });
    }
}