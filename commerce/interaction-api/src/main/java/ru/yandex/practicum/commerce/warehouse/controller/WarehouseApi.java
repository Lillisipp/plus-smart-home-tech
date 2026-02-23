package ru.yandex.practicum.commerce.warehouse.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.cart.dto.ShoppingCartDto;
import ru.yandex.practicum.commerce.warehouse.dto.AddProductToWarehouseRequest;
import ru.yandex.practicum.commerce.warehouse.dto.AddressDto;
import ru.yandex.practicum.commerce.warehouse.dto.BookedProductsDto;
import ru.yandex.practicum.commerce.warehouse.dto.NewProductInWarehouseRequest;

@RequestMapping("/api/v1/warehouse")
public interface WarehouseApi {
    @PutMapping
    void newProductInWarehouse(@RequestBody @Valid NewProductInWarehouseRequest request);

    @PostMapping("/add")
    void addProductToWarehouse(@RequestBody @Valid AddProductToWarehouseRequest request);

    @PostMapping("/check")
    BookedProductsDto checkProductQuantityEnoughForShoppingCart(@RequestBody @Valid ShoppingCartDto shoppingCart);

    @GetMapping("/address")
    AddressDto getWarehouseAddress();
}
