package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.cart.dto.ShoppingCartDto;
import ru.yandex.practicum.commerce.warehouse.controller.WarehouseApi;
import ru.yandex.practicum.commerce.warehouse.dto.AddProductToWarehouseRequest;
import ru.yandex.practicum.commerce.warehouse.dto.AddressDto;
import ru.yandex.practicum.commerce.warehouse.dto.BookedProductsDto;
import ru.yandex.practicum.commerce.warehouse.dto.NewProductInWarehouseRequest;
import ru.yandex.practicum.service.WarehouseService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/warehouse")
public class WarehouseController implements WarehouseApi {

    private final WarehouseService warehouseService;

    @Override
    public void newProductInWarehouse(@Valid NewProductInWarehouseRequest request) {
        log.info("ENTER controller newProductInWarehouse");
        warehouseService.newProductInWarehouse(request);
        log.info("EXIT controller newProductInWarehouse");
    }

    @Override
    public void addProductToWarehouse(@Valid AddProductToWarehouseRequest request) {
        log.info("ENTER controller addProductToWarehouse");
        warehouseService.addProductToWarehouse(request);
        log.info("EXIT controller addProductToWarehouse");
    }

    @Override
    public BookedProductsDto checkProductQuantityEnoughForShoppingCart(ShoppingCartDto shoppingCart) {
        log.info("ENTER controller checkProductQuantityEnoughForShoppingCart");
        return warehouseService.checkProductQuantityEnoughForShoppingCart(shoppingCart);

    }

    @Override
    public AddressDto getWarehouseAddress() {
        log.info("ENTER controller getWarehouseAddress");
        return warehouseService.getWarehouseAddress();
    }
}