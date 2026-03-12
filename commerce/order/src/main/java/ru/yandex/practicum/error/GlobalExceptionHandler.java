package ru.yandex.practicum.error;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.commerce.error.ApiErrorDto;

import java.time.Instant;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotAuthorizedUserException.class)
    public ResponseEntity<ApiErrorDto> handleNotAuthorized(NotAuthorizedUserException e, HttpServletRequest req) {
        log.warn("NotAuthorizedUserException: path={}, message={}", req.getRequestURI(), e.getMessage());

        ApiErrorDto body = new ApiErrorDto(
                Instant.now(),
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                e.getMessage(),
                req.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(NoOrderFoundException.class)
    public ResponseEntity<ApiErrorDto> handleNoOrderFound(NoOrderFoundException e, HttpServletRequest req) {
        log.warn("NoOrderFoundException: path={}, message={}", req.getRequestURI(), e.getMessage());

        ApiErrorDto body = new ApiErrorDto(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                e.getMessage(),
                req.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorDto> handleOther(Exception e, HttpServletRequest req) {
        log.error("Unhandled exception: path={}", req.getRequestURI(), e);

        ApiErrorDto body = new ApiErrorDto(
                Instant.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "Internal error",
                req.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
