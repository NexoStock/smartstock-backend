package com.nexostock.backend.shared.interfaces.rest.transform;

import com.nexostock.backend.shared.application.result.ApplicationError;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ProblemDetailFromApplicationErrorAssembler {

    private final MessageSource messageSource;

    public ProblemDetailFromApplicationErrorAssembler(
            MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public ResponseEntity<ProblemDetail> toResponseEntity(
            ApplicationError error) {

        HttpStatus status = statusOf(error.code());

        String title = messageSource.getMessage(
                "error." + error.code(),
                null,
                error.message(),
                LocaleContextHolder.getLocale()
        );

        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(status, error.detail());

        problem.setTitle(title);
        problem.setProperty("code", error.code());

        return ResponseEntity.status(status).body(problem);
    }

    private HttpStatus statusOf(String code) {
        return switch (code) {
            case ApplicationError.VALIDATION_ERROR ->
                    HttpStatus.BAD_REQUEST;

            case ApplicationError.NOT_FOUND ->
                    HttpStatus.NOT_FOUND;

            case ApplicationError.BUSINESS_RULE_VIOLATION ->
                    HttpStatus.UNPROCESSABLE_ENTITY;

            case ApplicationError.CONFLICT ->
                    HttpStatus.CONFLICT;

            case ApplicationError.UNAUTHORIZED ->
                    HttpStatus.UNAUTHORIZED;

            default ->
                    HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}