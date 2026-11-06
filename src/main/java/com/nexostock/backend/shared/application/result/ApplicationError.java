
package com.nexostock.backend.shared.application.result;

/**
 * Business error with a code, a default message and a detail.
 * Error de negocio.
 */
public record ApplicationError(
        String code,
        String message,
        String detail) {

    public static final String VALIDATION_ERROR =
            "VALIDATION_ERROR";

    public static final String NOT_FOUND =
            "NOT_FOUND";

    public static final String BUSINESS_RULE_VIOLATION =
            "BUSINESS_RULE_VIOLATION";

    public static final String CONFLICT =
            "CONFLICT";

    public static final String UNAUTHORIZED =
            "UNAUTHORIZED";

    public static final String UNEXPECTED_ERROR =
            "UNEXPECTED_ERROR";

    public static ApplicationError validationError(String detail) {
        return new ApplicationError(
                VALIDATION_ERROR,
                "Validation error",
                detail
        );
    }

    public static ApplicationError notFound(String detail) {
        return new ApplicationError(
                NOT_FOUND,
                "Resource not found",
                detail
        );
    }

    public static ApplicationError businessRuleViolation(
            String detail) {
        return new ApplicationError(
                BUSINESS_RULE_VIOLATION,
                "Business rule violation",
                detail
        );
    }

    public static ApplicationError conflict(String detail) {
        return new ApplicationError(
                CONFLICT,
                "Conflict",
                detail
        );
    }

    public static ApplicationError unauthorized(String detail) {
        return new ApplicationError(
                UNAUTHORIZED,
                "Unauthorized",
                detail
        );
    }

    public static ApplicationError unexpectedError(String detail) {
        return new ApplicationError(
                UNEXPECTED_ERROR,
                "Unexpected error",
                detail
        );
    }
}
