package ru.yandex.practicum.controller;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.commerce.order.dto.ProductPriceDto;
import ru.yandex.practicum.commerce.store.controller.ShoppingStoreApi;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "store")
public interface ShoppingStoreFeignClient extends ShoppingStoreApi {
    @PostMapping("/prices")
    List<ProductPriceDto> getPrices(@RequestBody List<UUID> productIds);
}