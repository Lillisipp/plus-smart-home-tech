package ru.yandex.practicum.controller;

import feign.FeignException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.cart.dto.ShoppingCartDto;
import ru.yandex.practicum.commerce.warehouse.controller.WarehouseApi;
import ru.yandex.practicum.commerce.warehouse.dto.*;
import ru.yandex.practicum.service.WarehouseService;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
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

    @Override
    public BookedProductsDto assemblyProductsForOrder(AssemblyProductsForOrderRequest request) {
        log.info("ENTER assemblyProductsForOrder: orderId={}, items={}",
                request == null ? null : request.getOrderId(),
                request == null || request.getProducts() == null ? 0 : request.getProducts().size());
        BookedProductsDto result = warehouseService.assemblyProductsForOrder(request);
        log.info("EXIT assemblyProductsForOrder: orderId={}, result={}",
                request == null ? null : request.getOrderId(), result);
        return result;
    }


    @Override
    public void shippedToDelivery(ShippedToDeliveryRequest request) {
        log.info("ENTER shippedToDelivery: orderId={}, deliveryId={}",
                request == null ? null : request.getOrderId(),
                request == null ? null : request.getDeliveryId());
        warehouseService.shippedToDelivery(request);
        log.info("EXIT shippedToDelivery: orderId={}, deliveryId={}",
                request == null ? null : request.getOrderId(),
                request == null ? null : request.getDeliveryId());
    }

    @Override
    public void acceptReturn(AcceptReturnRequest products) {
        int items = (products == null || products.getProducts() == null) ? 0 : products.getProducts().size();
        log.info("ENTER acceptReturn: items={}", items);
        warehouseService.acceptReturn(products);
        log.info("EXIT acceptReturn");
    }
}