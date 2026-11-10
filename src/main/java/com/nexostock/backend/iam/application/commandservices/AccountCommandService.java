package com.nexostock.backend.iam.application.commandservices;

import com.nexostock.backend.iam.domain.model.aggregates.Account;
import com.nexostock.backend.iam.domain.model.commands.RegisterBusinessCommand;
import com.nexostock.backend.iam.domain.model.commands.SignInCommand;
import com.nexostock.backend.shared.application.result.ApplicationError;
import com.nexostock.backend.shared.application.result.Result;

/** 2 commands defined = 2 handled. */
public interface AccountCommandService {

    /** Account + business name + generated token. */
    record SignedIn(Account account, String businessName, String businessType, String token) {}

    Result<SignedIn, ApplicationError> handle(RegisterBusinessCommand command);

    Result<SignedIn, ApplicationError> handle(SignInCommand command);
}
