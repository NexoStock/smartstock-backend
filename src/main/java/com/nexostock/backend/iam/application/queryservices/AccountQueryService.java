package com.nexostock.backend.iam.application.queryservices;

import com.nexostock.backend.iam.domain.model.aggregates.Account;
import com.nexostock.backend.iam.domain.model.queries.GetAccountByIdQuery;
import com.nexostock.backend.shared.application.result.ApplicationError;
import com.nexostock.backend.shared.application.result.Result;

/** 1 query defined = 1 handled. */
public interface AccountQueryService {
    Result<Account, ApplicationError> handle(GetAccountByIdQuery query);
}
