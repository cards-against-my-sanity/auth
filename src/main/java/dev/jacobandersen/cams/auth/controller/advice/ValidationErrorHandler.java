package dev.jacobandersen.cams.auth.controller.advice;

import dev.jacobandersen.cams.auth.dto.out.ErrorDto;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.List;

@RestControllerAdvice
public class ValidationErrorHandler {
    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<ErrorDto> handleValidationError(MethodArgumentNotValidException ex) {
        final List<String> errors = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(ObjectError::getDefaultMessage)
                .toList();

        final HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
        return ResponseEntity.status(status).body(new ErrorDto(status, errors));
    }

    @ExceptionHandler({MissingServletRequestParameterException.class})
    public ResponseEntity<ErrorDto> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
        final HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
        return ResponseEntity.status(status).body(new ErrorDto(status, List.of(ex.getMessage())));
    }

    @ExceptionHandler({HandlerMethodValidationException.class})
    public ResponseEntity<ErrorDto> handleValidationError(HandlerMethodValidationException ex) {
        final List<String> errors = ex.getAllErrors()
                .stream()
                .map(MessageSourceResolvable::getDefaultMessage)
                .toList();

        final HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
        return ResponseEntity.status(status).body(new ErrorDto(status, errors));
    }
}
