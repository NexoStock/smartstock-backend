package com.nexostock.backend.iam.application.internal.queryservices;

import com.nexostock.backend.iam.application.queryservices.AccountQueryService;
import com.nexostock.backend.iam.domain.model.aggregates.Account;
import com.nexostock.backend.iam.domain.model.queries.GetAccountByIdQuery;
import com.nexostock.backend.iam.infrastructure.persistence.jpa.AccountRepository;
import com.nexostock.backend.shared.application.result.ApplicationError;
import com.nexostock.backend.shared.application.result.Result;
import org.springframework.stereotype.Service;

@Service
public class AccountQueryServiceImpl implements AccountQueryService {

    private final AccountRepository accounts;

    public AccountQueryServiceImpl(AccountRepository accounts) {
        this.accounts = accounts;
    }

    @Override
    public Result<Account, ApplicationError> handle(GetAccountByIdQuery query) {
        return accounts
                .findById(query.accountId())
                .map(a -> Result.<Account, ApplicationError>success(a))
                .orElseGet(() -> Result.failure(ApplicationError.notFound("ACCOUNT_NOT_FOUND")));
    }
}
