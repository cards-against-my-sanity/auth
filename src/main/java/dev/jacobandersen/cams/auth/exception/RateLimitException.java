package dev.jacobandersen.cams.auth.exception;

import dev.jacobandersen.cams.auth.dto.out.ErrorDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class RateLimitException extends RuntimeException {
    public RateLimitException(String remoteAddr) {
        super(String.format("rate limit exceeded for %s", remoteAddr));
    }

    public ErrorDto toErrorDto() {
        return new ErrorDto(HttpStatus.TOO_MANY_REQUESTS, List.of(getMessage()));
    }
}
