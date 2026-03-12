package ru.yandex.practicum.error;

public class DeliveryNotFoundException extends RuntimeException {
    public DeliveryNotFoundException(String message) { super(message); }
}