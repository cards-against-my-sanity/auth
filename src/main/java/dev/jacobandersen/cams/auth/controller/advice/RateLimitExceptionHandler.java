package dev.jacobandersen.cams.auth.controller.advice;

import dev.jacobandersen.cams.auth.dto.out.ErrorDto;
import dev.jacobandersen.cams.auth.exception.RateLimitException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RateLimitExceptionHandler {
    @ExceptionHandler({RateLimitException.class})
    public ResponseEntity<ErrorDto> handleRateLimitException(RateLimitException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(ex.toErrorDto());
    }
}
