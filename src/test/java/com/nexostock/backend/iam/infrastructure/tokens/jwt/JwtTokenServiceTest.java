package com.nexostock.backend.iam.infrastructure.tokens.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenServiceTest {

    private static final String SECRET =
            "nexostock-jwt-test-secret-1234567890-abcdef-1234567890";

    private final JwtTokenService service =
            new JwtTokenService(SECRET, 8);

    private final SecretKey key =
            Keys.hmacShaKeyFor(
                    SECRET.getBytes(StandardCharsets.UTF_8)
            );

    @Test
    void validTokenReturnsAuthenticatedAccount() {
        String token = createToken(
                "11",
                22L,
                "usuario@nexostock.com",
                Instant.now().plusSeconds(3600)
        );

        var result = service.validate(token);

        assertTrue(result.isPresent());
        assertEquals(11L, result.get().accountId());
        assertEquals(22L, result.get().businessId());
        assertEquals(
                "usuario@nexostock.com",
                result.get().email()
        );
    }

    @Test
    void expiredTokenIsRejected() {
        String token = createToken(
                "11",
                22L,
                "usuario@nexostock.com",
                Instant.now().minusSeconds(10)
        );

        assertTrue(service.validate(token).isEmpty());
    }

    @Test
    void modifiedSignatureIsRejected() {
        String token = createToken(
                "11",
                22L,
                "usuario@nexostock.com",
                Instant.now().plusSeconds(3600)
        );

        String[] parts = token.split("\\.");

        String signature = parts[2];

        parts[2] =
                (signature.startsWith("A") ? "B" : "A")
                        + signature.substring(1);

        String modified = String.join(".", parts);

        assertTrue(service.validate(modified).isEmpty());
    }

    @Test
    void malformedTokenIsRejected() {
        assertTrue(
                service.validate("invalid.jwt.token").isEmpty()
        );
    }

    @Test
    void invalidAccountIdIsRejected() {
        String token = createToken(
                "not-a-number",
                22L,
                "usuario@nexostock.com",
                Instant.now().plusSeconds(3600)
        );

        assertTrue(service.validate(token).isEmpty());
    }

    @Test
    void missingBusinessIdIsRejected() {
        String token = createToken(
                "11",
                null,
                "usuario@nexostock.com",
                Instant.now().plusSeconds(3600)
        );

        assertTrue(service.validate(token).isEmpty());
    }

    @Test
    void missingEmailIsRejected() {
        String token = createToken(
                "11",
                22L,
                null,
                Instant.now().plusSeconds(3600)
        );

        assertTrue(service.validate(token).isEmpty());
    }

    private String createToken(
            String subject,
            Long businessId,
            String email,
            Instant expiration) {

        var builder = Jwts.builder()
                .subject(subject)
                .issuedAt(
                        Date.from(Instant.now().minusSeconds(60))
                )
                .expiration(Date.from(expiration));

        if (businessId != null) {
            builder.claim("businessId", businessId);
        }

        if (email != null) {
            builder.claim("email", email);
        }

        return builder
                .signWith(key)
                .compact();
    }
}