package ru.yandex.practicum.commerce.delivery.controller;

import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.yandex.practicum.commerce.delivery.dto.DeliveryDto;
import ru.yandex.practicum.commerce.delivery.enums.DeliveryState;
import ru.yandex.practicum.commerce.order.dto.OrderDto;

import java.math.BigDecimal;
import java.util.UUID;

@Validated
@RequestMapping("/api/v1/delivery")
public interface DeliveryApi {
    @PostMapping
    DeliveryDto planDelivery(@RequestBody @Valid DeliveryDto request);

    // рассчитать стоимость доставки
    @PostMapping("/cost")
    BigDecimal deliveryCost(@RequestBody @Valid OrderDto request);

    // принять товары в доставку (IN_PROGRESS)
    @PostMapping("/picked")
    void deliveryPicked(@RequestBody @Valid UUID orderId);

    // успешная доставка (DELIVERED)
    @PostMapping("/success")
    void deliverySuccess(@RequestBody @Valid UUID orderId);

    // ошибка доставки (FAILED)
    @PostMapping("/failed")
    void deliveryFailed(@RequestBody @Valid UUID orderId);
}