package com.nexostock.backend.iam.infrastructure.authorization;

import com.nexostock.backend.iam.application.internal.outboundservices.tokens.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenService tokens;

    public JwtAuthenticationFilter(TokenService tokens) {
        this.tokens = tokens;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        return path.equals("/api/auth/register")
                || path.equals("/api/auth/login");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws ServletException, IOException {

        String authorization =
                request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorization == null || authorization.isBlank()) {
            chain.doFilter(request, response);
            return;
        }

        if (!authorization.startsWith(BEARER_PREFIX)) {
            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Invalid authorization header"
            );
            return;
        }

        String token =
                authorization.substring(BEARER_PREFIX.length()).trim();

        var authenticated = tokens.validate(token);

        if (authenticated.isEmpty()) {
            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Invalid or expired token"
            );
            return;
        }

        AuthenticatedAccount account = authenticated.get();

        if (account.accountId() == null
                || account.accountId() <= 0
                || account.businessId() == null
                || account.businessId() <= 0
                || account.email() == null
                || account.email().isBlank()) {

            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Invalid token claims"
            );
            return;
        }

        var authentication =
                new UsernamePasswordAuthenticationToken(
                        account,
                        null,
                        List.of()
                );

        SecurityContext context =
                SecurityContextHolder.createEmptyContext();

        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);

        try {
            chain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}