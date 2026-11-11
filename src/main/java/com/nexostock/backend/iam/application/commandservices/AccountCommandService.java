package com.nexostock.backend.iam.application.commandservices;

import com.nexostock.backend.iam.domain.model.aggregates.Account;
import com.nexostock.backend.iam.domain.model.commands.RegisterBusinessCommand;
import com.nexostock.backend.iam.domain.model.commands.SignInCommand;
import com.nexostock.backend.shared.application.result.ApplicationError;
import com.nexostock.backend.shared.application.result.Result;
import com.nexostock.backend.iam.domain.model.commands.RequestPasswordResetCommand;
import com.nexostock.backend.iam.domain.model.commands.ResetPasswordCommand;

/** 4 commands defined = 4 handled. */public interface AccountCommandService {

    /** Account + business name + generated token. */
    record SignedIn(Account account, String businessName, String businessType, String token) {}

    Result<SignedIn, ApplicationError> handle(RegisterBusinessCommand command);

    Result<SignedIn, ApplicationError> handle(SignInCommand command);

    Result<Boolean, ApplicationError> handle(RequestPasswordResetCommand command);

    Result<Boolean, ApplicationError> handle(ResetPasswordCommand command);
}
