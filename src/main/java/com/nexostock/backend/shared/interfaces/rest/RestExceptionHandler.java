
package com.nexostock.backend.shared.interfaces.rest;

import com.nexostock.backend.shared.application.result.ApplicationError;
import com.nexostock.backend.shared.interfaces.rest.transform.ProblemDetailFromApplicationErrorAssembler;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for the whole application.
 * Maneja los errores de toda la aplicación.
 */
@RestControllerAdvice
public class RestExceptionHandler {

    private final ProblemDetailFromApplicationErrorAssembler problems;

    public RestExceptionHandler(
            ProblemDetailFromApplicationErrorAssembler problems) {
        this.problems = problems;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> onInvalidBody(
            MethodArgumentNotValidException ex) {

        String detail = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));

        return problems.toResponseEntity(
                ApplicationError.validationError(detail)
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> onUnreadableBody(
            HttpMessageNotReadableException ex) {

        return problems.toResponseEntity(
                ApplicationError.validationError("Malformed JSON body")
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> onConstraint(
            DataIntegrityViolationException ex) {

        return problems.toResponseEntity(
                ApplicationError.conflict(
                        "DUPLICATE_OR_CONSTRAINT_VIOLATION"
                )
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> onInvalidValue(
            IllegalArgumentException ex) {

        return problems.toResponseEntity(
                ApplicationError.validationError(ex.getMessage())
        );
    }
}
