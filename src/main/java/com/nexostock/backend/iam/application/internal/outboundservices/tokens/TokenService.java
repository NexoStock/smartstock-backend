package com.nexostock.backend.iam.application.internal.outboundservices.tokens;

import com.nexostock.backend.iam.domain.model.aggregates.Account;
import com.nexostock.backend.iam.infrastructure.authorization.AuthenticatedAccount;
import java.util.Optional;

public interface TokenService {
    String generateToken(Account account);

    /** Returns the account data if the token is valid and not expired. */
    Optional<AuthenticatedAccount> validate(String token);
}
