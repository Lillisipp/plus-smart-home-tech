package ru.yandex.practicum.commerce.payment.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.yandex.practicum.commerce.order.dto.OrderDto;
import ru.yandex.practicum.commerce.payment.dto.PaymentDto;

import java.math.BigDecimal;
import java.util.UUID;

@Validated
@RequestMapping("/api/v1/payment")
public interface PaymentApi {

    @PostMapping("/productCost")
    BigDecimal productCost(@RequestBody @Valid OrderDto request);

    @PostMapping("/totalCost")
    BigDecimal getTotalCost(@RequestBody @Valid OrderDto request);

    @PostMapping
    PaymentDto payment(@RequestBody @Valid OrderDto request);

    @PostMapping("/success")
    void paymentSuccess(@RequestBody @NotNull UUID paymentId);

    @PostMapping("/failed")
    void paymentFailed(@RequestBody @NotNull UUID paymentId);
}

