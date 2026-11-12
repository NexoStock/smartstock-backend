package com.nexostock.backend.iam.application.internal.commandservices;

import com.nexostock.backend.iam.application.commandservices.AccountCommandService;
import com.nexostock.backend.iam.application.internal.outboundservices.hashing.HashingService;
import com.nexostock.backend.iam.application.internal.outboundservices.hashing.ResetTokenService;
import com.nexostock.backend.iam.application.internal.outboundservices.notifications.PasswordResetNotifier;
import com.nexostock.backend.iam.application.internal.outboundservices.tokens.TokenService;
import com.nexostock.backend.iam.domain.model.aggregates.Account;
import com.nexostock.backend.iam.domain.model.aggregates.Business;
import com.nexostock.backend.iam.domain.model.aggregates.PasswordResetToken;
import com.nexostock.backend.iam.domain.model.commands.RegisterBusinessCommand;
import com.nexostock.backend.iam.domain.model.commands.RequestPasswordResetCommand;
import com.nexostock.backend.iam.domain.model.commands.ResetPasswordCommand;
import com.nexostock.backend.iam.domain.model.commands.SignInCommand;
import com.nexostock.backend.iam.infrastructure.persistence.jpa.AccountRepository;
import com.nexostock.backend.iam.infrastructure.persistence.jpa.BusinessRepository;
import com.nexostock.backend.iam.infrastructure.persistence.jpa.PasswordResetTokenRepository;
import com.nexostock.backend.shared.application.result.ApplicationError;
import com.nexostock.backend.shared.application.result.Result;
import java.time.Duration;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountCommandServiceImpl implements AccountCommandService {

    private static final Duration RESET_TOKEN_TTL = Duration.ofHours(1);

    private final AccountRepository accounts;
    private final BusinessRepository businesses;
    private final HashingService hashing;
    private final TokenService tokens;
    private final PasswordResetTokenRepository resetTokens;
    private final ResetTokenService resetTokenService;
    private final PasswordResetNotifier notifier;

    public AccountCommandServiceImpl(
            AccountRepository accounts,
            BusinessRepository businesses,
            HashingService hashing,
            TokenService tokens,
            PasswordResetTokenRepository resetTokens,
            ResetTokenService resetTokenService,
            PasswordResetNotifier notifier) {
        this.accounts = accounts;
        this.businesses = businesses;
        this.hashing = hashing;
        this.tokens = tokens;
        this.resetTokens = resetTokens;
        this.resetTokenService = resetTokenService;
        this.notifier = notifier;
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

    @Override
    @Transactional
    public Result<Boolean, ApplicationError> handle(RequestPasswordResetCommand command) {
        // Always succeeds, whether the email exists or not (no account enumeration)
        accounts.findByEmail(command.email()).ifPresent(account -> {
            String rawToken = resetTokenService.generateRawToken();
            resetTokens.save(new PasswordResetToken(
                    account.getId(), resetTokenService.hash(rawToken), Instant.now().plus(RESET_TOKEN_TTL)));
            notifier.sendResetLink(account.getEmail().value(), rawToken);
        });
        return Result.success(true);
    }

    @Override
    @Transactional
    public Result<Boolean, ApplicationError> handle(ResetPasswordCommand command) {
        Instant now = Instant.now();
        var token = resetTokens.findByTokenHash(resetTokenService.hash(command.token()));
        if (token.isEmpty() || !token.get().isUsable(now)) {
            return Result.failure(ApplicationError.validationError("INVALID_OR_EXPIRED_TOKEN"));
        }
        var account = accounts.findById(token.get().getAccountId());
        if (account.isEmpty()) {
            return Result.failure(ApplicationError.validationError("INVALID_OR_EXPIRED_TOKEN"));
        }
        account.get().changePasswordHash(hashing.encode(command.newRawPassword()));
        token.get().markUsed(now);
        return Result.success(true);
    }
}