package com.nexostock.backend.iam.infrastructure.tokens.jwt;

import com.nexostock.backend.iam.application.internal.outboundservices.tokens.TokenService;
import com.nexostock.backend.iam.domain.model.aggregates.Account;
import com.nexostock.backend.iam.infrastructure.authorization.AuthenticatedAccount;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Creates and validates JWT tokens (Crea y valida tokens JWT, duración 8 h). */
@Service
public class JwtTokenService implements TokenService {

    private final SecretKey key;
    private final Duration lifetime;

    public JwtTokenService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-hours}") long hours) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); // at least 32 bytes
        this.lifetime = Duration.ofHours(hours);
    }

    @Override
    public String generateToken(Account account) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(account.getId()))
                .claim("businessId", account.getBusinessId())
                .claim("email", account.getEmail().value())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(lifetime)))
                .signWith(key)
                .compact();
    }

    @Override
    public Optional<AuthenticatedAccount> validate(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            return Optional.of(
                    new AuthenticatedAccount(
                            Long.valueOf(claims.getSubject()),
                            claims.get("businessId", Long.class),
                            claims.get("email", String.class)));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
