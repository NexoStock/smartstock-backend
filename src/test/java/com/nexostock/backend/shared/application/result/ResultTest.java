
package com.nexostock.backend.shared.application.result;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ResultTest {

    @Test
    void successFoldsToTheSuccessBranch() {

        Result<Integer, ApplicationError> result =
                Result.success(5);

        assertTrue(result.isSuccess());

        assertEquals(
                "ok-5",
                result.<String>fold(
                        v -> "ok-" + v,
                        e -> "error"
                )
        );
    }

    @Test
    void failureFoldsToTheFailureBranch() {

        Result<Integer, ApplicationError> result =
                Result.failure(
                        ApplicationError.notFound("x")
                );

        assertTrue(result.isFailure());

        assertEquals(
                "NOT_FOUND",
                result.<String>fold(
                        v -> "ok",
                        ApplicationError::code
                )
        );
    }
}
