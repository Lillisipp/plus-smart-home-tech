package ru.yandex.practicum.controller;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.commerce.warehouse.controller.WarehouseApi;

@FeignClient(name = "warehouse",path = "/api/v1/warehouse")
public interface WarehouseClient extends WarehouseApi {

}
