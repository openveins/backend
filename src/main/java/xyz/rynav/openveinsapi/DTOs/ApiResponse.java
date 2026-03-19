package xyz.rynav.openveinsapi.DTOs;

import java.time.Instant;


public sealed interface ApiResponse<T> permits ApiResponse.Success, ApiResponse.Failure {

    record Success<T>(
        boolean success,
        String message,
        String timestamp,
        T data
    ) implements ApiResponse<T> {
        public Success(String message, T data){
            this(true, message, Instant.now().toString(), data);
        }
    }

    record Failure<T>(
            boolean success,
            String message,
            String timestamp
    ) implements ApiResponse<T> {
        public Failure(String message){
            this(false, message, Instant.now().toString());
        }
    }

    static <T> Success<T> ok(String message, T data){
        return new Success<>(message, data);
    }

    static <T> Failure<T> fail(String message){
        return new Failure<>(message);
    }
}
