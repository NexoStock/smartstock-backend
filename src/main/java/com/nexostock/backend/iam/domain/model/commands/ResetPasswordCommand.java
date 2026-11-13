package com.nexostock.backend.iam.domain.model.commands;

public record ResetPasswordCommand(String token, String newRawPassword) {}