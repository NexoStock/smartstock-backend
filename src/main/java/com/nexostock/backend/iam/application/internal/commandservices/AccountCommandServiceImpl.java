package com.nexostock.backend.iam.application.internal.commandservices;

import com.nexostock.backend.iam.application.commandservices.AccountCommandService;
import com.nexostock.backend.iam.application.internal.outboundservices.hashing.HashingService;
import com.nexostock.backend.iam.application.internal.outboundservices.tokens.TokenService;
import com.nexostock.backend.iam.domain.model.aggregates.Account;
import com.nexostock.backend.iam.domain.model.aggregates.Business;
import com.nexostock.backend.iam.domain.model.commands.RegisterBusinessCommand;
import com.nexostock.backend.iam.domain.model.commands.SignInCommand;
import com.nexostock.backend.iam.infrastructure.persistence.jpa.AccountRepository;
import com.nexostock.backend.iam.infrastructure.persistence.jpa.BusinessRepository;
import com.nexostock.backend.shared.application.result.ApplicationError;
import com.nexostock.backend.shared.application.result.Result;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountCommandServiceImpl implements AccountCommandService {

    private final AccountRepository accounts;
    private final BusinessRepository businesses;
    private final HashingService hashing;
    private final TokenService tokens;

    public AccountCommandServiceImpl(
            AccountRepository accounts,
            BusinessRepository businesses,
            HashingService hashing,
            TokenService tokens) {
        this.accounts = accounts;
        this.businesses = businesses;
        this.hashing = hashing;
        this.tokens = tokens;
    }

    @Override
    @Transactional
    public Result<SignedIn, ApplicationError> handle(RegisterBusinessCommand command) {
        if (accounts.existsByEmail(command.email())) {
            return Result.failure(ApplicationError.conflict("EMAIL_ALREADY_REGISTERED"));
        }
        Business business = businesses.save(new Business(command.businessName(), command.businessType(), command.contactPhone()));
        Account account =
                accounts.save(
                        new Account(business.getId(), command.email(), hashing.encode(command.rawPassword())));
        return Result.success(
                new SignedIn(account, business.getName(), business.getBusinessType(), tokens.generateToken(account)));
    }

    @Override
    @Transactional(readOnly = true)
    public Result<SignedIn, ApplicationError> handle(SignInCommand command) {
        var account = accounts.findByEmail(command.email());
        // Same error for "no email" and "wrong password" (no account enumeration)
        if (account.isEmpty() || !hashing.matches(command.rawPassword(), account.get().getPasswordHash())) {
            return Result.failure(ApplicationError.validationError("INVALID_CREDENTIALS"));
        }
        Business business = businesses.findById(account.get().getBusinessId()).orElseThrow();
        return Result.success(
                new SignedIn(account.get(), business.getName(), business.getBusinessType(),
                        tokens.generateToken(account.get())));
    }
}
