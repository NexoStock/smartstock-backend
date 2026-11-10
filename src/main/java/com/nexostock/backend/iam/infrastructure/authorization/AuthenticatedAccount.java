package com.nexostock.backend.iam.infrastructure.authorization;

/** Data stored in the security context after validating a JWT. */
public record AuthenticatedAccount(Long accountId, Long businessId, String email) {}
