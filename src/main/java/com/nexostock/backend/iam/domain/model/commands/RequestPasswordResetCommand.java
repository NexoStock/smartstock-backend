package com.nexostock.backend.iam.domain.model.commands;

import com.nexostock.backend.iam.domain.model.valueobjects.EmailAddress;

public record RequestPasswordResetCommand(EmailAddress email) {}