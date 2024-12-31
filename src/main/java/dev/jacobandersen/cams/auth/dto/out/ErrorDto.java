package dev.jacobandersen.cams.auth.dto.out;

import org.springframework.http.HttpStatus;

import java.util.List;

public record ErrorDto(HttpStatus status, List<String> errors) {
}
