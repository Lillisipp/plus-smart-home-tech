package ru.yandex.practicum.commerce.error;

import java.time.Instant;

public record ApiErrorDto(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {}