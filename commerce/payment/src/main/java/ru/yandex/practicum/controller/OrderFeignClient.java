package ru.yandex.practicum.controller;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.commerce.order.controller.OrderApi;

@SpringBootApplication
@EnableDiscoveryClient
@FeignClient(name = "order")
public interface OrderFeignClient extends OrderApi {
}
