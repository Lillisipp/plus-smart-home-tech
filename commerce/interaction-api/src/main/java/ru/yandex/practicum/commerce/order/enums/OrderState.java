package ru.yandex.practicum.commerce.order.enums;

public enum OrderState {
    NEW,
    ON_PAYMENT,
    ON_DELIVERY,
    ASSEMBLED,
    PAID,
    DELIVERED,
    COMPLETED,
    DELIVERY_FAILED,
    ASSEMBLY_FAILED,
    PAYMENT_FAILED,
    PRODUCT_RETURNED,
    CANCELED
}
