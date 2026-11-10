package com.nexostock.backend.shared.interfaces.rest.security;

import com.nexostock.backend.iam.infrastructure.authorization.AuthenticatedAccount;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class JwtCurrentBusinessProvider
        implements CurrentBusinessProvider {

    @Override
    public Long businessId() {
        return currentAccount().businessId();
    }

    @Override
    public Long accountId() {
        return currentAccount().accountId();
    }

    private AuthenticatedAccount currentAccount() {

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal()
                        instanceof AuthenticatedAccount account)) {

            throw new AuthenticationCredentialsNotFoundException(
                    "JWT_AUTHENTICATION_REQUIRED"
            );
        }

        if (account.accountId() == null
                || account.accountId() <= 0
                || account.businessId() == null
                || account.businessId() <= 0) {

            throw new AuthenticationCredentialsNotFoundException(
                    "INVALID_AUTHENTICATED_ACCOUNT"
            );
        }

        return account;
    }
}