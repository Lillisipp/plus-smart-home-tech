package ru.yandex.practicum.controller;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.commerce.payment.controller.PaymentApi;

@FeignClient(name = "payment")
public interface PaymentFeignClient extends PaymentApi {
}
