package com.nexostock.backend.iam.domain.model.queries;

import java.util.Objects;

/** Query: one account by id. */
public record GetAccountByIdQuery(Long accountId) {
    public GetAccountByIdQuery {
        Objects.requireNonNull(accountId, "accountId is required");
    }
}
