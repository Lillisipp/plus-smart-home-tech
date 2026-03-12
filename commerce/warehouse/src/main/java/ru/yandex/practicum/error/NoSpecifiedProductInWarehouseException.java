package ru.yandex.practicum.error;

public class NoSpecifiedProductInWarehouseException extends RuntimeException{
    public NoSpecifiedProductInWarehouseException(String message) { super(message); }
}