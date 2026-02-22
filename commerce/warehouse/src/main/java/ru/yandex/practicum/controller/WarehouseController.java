package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.cart.dto.ShoppingCartDto;
import ru.yandex.practicum.commerce.warehouse.controller.WarehouseApi;
import ru.yandex.practicum.commerce.warehouse.dto.AddProductToWarehouseRequest;
import ru.yandex.practicum.commerce.warehouse.dto.AddressDto;
import ru.yandex.practicum.commerce.warehouse.dto.BookedProductsDto;
import ru.yandex.practicum.commerce.warehouse.dto.NewProductInWarehouseRequest;

@Slf4j
@RestController
@RequestMapping("/warehouse")
public class WarehouseController implements WarehouseApi {


    @Override
    public void newProductInWarehouse(@Valid NewProductInWarehouseRequest request) {

    }

    @Override
    public void addProductToWarehouse(@Valid AddProductToWarehouseRequest request) {

    }

    @Override
    public BookedProductsDto checkProductQuantityEnoughForShoppingCart(ShoppingCartDto shoppingCart) {
        return null;
    }

    @Override
    public AddressDto getWarehouseAddress() {
        return null;
    }
}