package ru.yandex.practicum.error;

public class NotAuthorizedUserException extends RuntimeException {
    public NotAuthorizedUserException(String message) { super(message); }
}