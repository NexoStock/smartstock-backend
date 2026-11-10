package com.nexostock.backend.iam.infrastructure.authorization;

import com.nexostock.backend.iam.application.internal.outboundservices.tokens.TokenService;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    private final TokenService tokens = mock(TokenService.class);
    private final JwtAuthenticationFilter filter =
            new JwtAuthenticationFilter(tokens);

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void validTokenAuthenticatesAccount() throws Exception {
        when(tokens.validate("valid-token")).thenReturn(
                Optional.of(new AuthenticatedAccount(
                        7L, 19L, "user@test.com"
                ))
        );

        var request = request("/api/productos");
        request.addHeader("Authorization", "Bearer valid-token");

        var response = new MockHttpServletResponse();
        var captured = new AtomicReference<AuthenticatedAccount>();

        filter.doFilter(request, response, (req, res) -> {
            var authentication =
                    SecurityContextHolder.getContext()
                            .getAuthentication();

            assertNotNull(authentication);
            assertTrue(authentication.isAuthenticated());

            captured.set(
                    (AuthenticatedAccount)
                            authentication.getPrincipal()
            );
        });

        assertNotNull(captured.get());
        assertEquals(7L, captured.get().accountId());
        assertEquals(19L, captured.get().businessId());

        assertNull(
                SecurityContextHolder.getContext()
                        .getAuthentication()
        );
    }

    @Test
    void invalidTokenReturns401() throws Exception {
        when(tokens.validate("invalid-token"))
                .thenReturn(Optional.empty());

        var request = request("/api/productos");
        request.addHeader("Authorization", "Bearer invalid-token");

        var response = new MockHttpServletResponse();
        var continued = new AtomicBoolean(false);

        filter.doFilter(request, response,
                (req, res) -> continued.set(true));

        assertEquals(401, response.getStatus());
        assertFalse(continued.get());
    }

    @Test
    void incorrectAuthorizationHeaderReturns401()
            throws Exception {

        var request = request("/api/productos");
        request.addHeader("Authorization", "Basic abc123");

        var response = new MockHttpServletResponse();
        var continued = new AtomicBoolean(false);

        filter.doFilter(request, response,
                (req, res) -> continued.set(true));

        assertEquals(401, response.getStatus());
        assertFalse(continued.get());
        verifyNoInteractions(tokens);
    }

    @Test
    void incompleteIdentityReturns401() throws Exception {
        when(tokens.validate("incomplete")).thenReturn(
                Optional.of(new AuthenticatedAccount(
                        7L, null, "user@test.com"
                ))
        );

        var request = request("/api/productos");
        request.addHeader("Authorization", "Bearer incomplete");

        var response = new MockHttpServletResponse();
        var continued = new AtomicBoolean(false);

        filter.doFilter(request, response,
                (req, res) -> continued.set(true));

        assertEquals(401, response.getStatus());
        assertFalse(continued.get());
    }

    @Test
    void missingTokenContinuesWithoutAuthentication()
            throws Exception {

        var request = request("/api/productos");
        var response = new MockHttpServletResponse();
        var continued = new AtomicBoolean(false);

        filter.doFilter(request, response, (req, res) -> {
            continued.set(true);
            assertNull(
                    SecurityContextHolder.getContext()
                            .getAuthentication()
            );
        });

        assertTrue(continued.get());
        verifyNoInteractions(tokens);
    }

    @Test
    void loginEndpointSkipsJwtValidation() throws Exception {
        var request = request("/api/auth/login");
        request.addHeader("Authorization", "Bearer invalid-token");

        var response = new MockHttpServletResponse();
        var continued = new AtomicBoolean(false);

        filter.doFilter(request, response,
                (req, res) -> continued.set(true));

        assertTrue(continued.get());
        verifyNoInteractions(tokens);
    }

    private MockHttpServletRequest request(String path) {
        var request = new MockHttpServletRequest("GET", path);
        request.setServletPath(path);
        return request;
    }
}