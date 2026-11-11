package com.nexostock.backend.iam.application.internal.outboundservices.notifications;

public interface PasswordResetNotifier {
    void sendResetLink(String email, String rawToken);
}