
package com.nexostock.backend.shared.application.result;

import java.util.Optional;
import java.util.function.Function;

/**
 * Result of a business operation:
 * success with a value or failure with an error.
 */
public sealed interface Result<T, E>
        permits Result.Success, Result.Failure {

    static <T, E> Result<T, E> success(T value) {
        return new Success<>(value);
    }

    static <T, E> Result<T, E> failure(E error) {
        return new Failure<>(error);
    }

    default boolean isSuccess() {
        return this instanceof Success<T, E>;
    }

    default boolean isFailure() {
        return this instanceof Failure<T, E>;
    }

    default Optional<T> getValue() {
        return this instanceof Success<T, E> s
                ? Optional.ofNullable(s.value())
                : Optional.empty();
    }

    default Optional<E> getError() {
        return this instanceof Failure<T, E> f
                ? Optional.ofNullable(f.error())
                : Optional.empty();
    }

    /**
     * Resolves the result into a single value.
     */
    default <R> R fold(
            Function<? super T, ? extends R> onSuccess,
            Function<? super E, ? extends R> onFailure) {

        if (this instanceof Success<T, E> s) {
            return onSuccess.apply(s.value());
        }

        return onFailure.apply(
                ((Failure<T, E>) this).error()
        );
    }

    /**
     * Transforms the value when it is a success.
     */
    default <R> Result<R, E> map(
            Function<? super T, ? extends R> mapper) {

        if (this instanceof Success<T, E> s) {
            return new Success<>(mapper.apply(s.value()));
        }

        return new Failure<>(
                ((Failure<T, E>) this).error()
        );
    }

    record Success<T, E>(T value)
            implements Result<T, E> {}

    record Failure<T, E>(E error)
            implements Result<T, E> {}
}
