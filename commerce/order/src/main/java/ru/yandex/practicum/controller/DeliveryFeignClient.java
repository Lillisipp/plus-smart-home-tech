package ru.yandex.practicum.controller;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.commerce.delivery.controller.DeliveryApi;

@FeignClient(name = "delivery")
public interface DeliveryFeignClient extends DeliveryApi {
}
