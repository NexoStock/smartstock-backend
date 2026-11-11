package com.nexostock.backend.alerts.infrastructure.notifications;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "app.notifications.enabled=false",
                "spring.datasource.url=jdbc:h2:mem:notifications_startup;MODE=MySQL;DB_CLOSE_DELAY=-1",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "app.jwt.secret=startup-test-only-secret-0123456789-abcdefghijklmnopqrstuvwxyz",
                "app.cors.allowed-origins=http://localhost:4200",
                "documentation.server.production-url=http://localhost:8080"
        })
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class NotificationsDisabledStartupTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private Environment environment;

    @Test
    void backendStartsWithoutSmtpWhenNotificationsAreDisabled() {
        assertThat(environment.getProperty("spring.mail.host")).isNull();
        assertThat(environment.getProperty("spring.mail.jndi-name")).isNull();

        assertThat(context.getBeansOfType(JavaMailSender.class)).isEmpty();
        assertThat(context.getBeansOfType(SmtpEmailSender.class)).isEmpty();
        assertThat(context.getBeansOfType(NotificationDispatcher.class)).isEmpty();

        assertThat(environment.getProperty("local.server.port", Integer.class))
                .isPositive();
    }
}