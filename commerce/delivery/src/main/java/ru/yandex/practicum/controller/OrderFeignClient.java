package ru.yandex.practicum.controller;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.commerce.order.controller.OrderApi;

@FeignClient(name = "order")
public interface OrderFeignClient extends OrderApi {
}
