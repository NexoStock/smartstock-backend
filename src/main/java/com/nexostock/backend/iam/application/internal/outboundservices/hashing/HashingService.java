package com.nexostock.backend.iam.application.internal.outboundservices.hashing;

public interface HashingService {
    String encode(String rawPassword);

    boolean matches(String rawPassword, String hash);
}
