package com.nexostock.backend.iam.application.internal.outboundservices.hashing;

public interface ResetTokenService {
    String generateRawToken();
    String hash(String rawToken);
}