package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.client.OrderClient;
import ru.yandex.practicum.commerce.order.controller.OrderApi;
import ru.yandex.practicum.commerce.order.dto.CreateNewOrderRequest;
import ru.yandex.practicum.commerce.order.dto.OrderDto;
import ru.yandex.practicum.commerce.order.dto.ProductPriceDto;
import ru.yandex.practicum.commerce.order.dto.ProductReturnRequest;
import ru.yandex.practicum.service.OrderService;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class OrderController implements OrderApi {

    private final OrderService orderService;
    @Override
    public List<OrderDto> getClientOrders(String username) {
        log.info("ENTER getClientOrders: username={}", username);
        List<OrderDto> result = orderService.getClientOrders(username);
        log.info("EXIT getClientOrders: username={}, count={}", username, result == null ? 0 : result.size());
        return result;
    }

    @Override
    public OrderDto createNewOrder(CreateNewOrderRequest request) {
        OrderDto result = orderService.createNewOrder(request);
        log.info("EXIT createNewOrder: orderId={}", result == null ? null : result.getOrderId());
        return result;
    }

    @Override
    public OrderDto productReturn(ProductReturnRequest request) {
        UUID orderId = request.getOrderId();
        int items = request.getProducts() == null ? 0 : request.getProducts().size();
        log.info("productReturn: orderId={}, items={}", orderId, items);

        OrderDto result = orderService.productReturn(request);

        log.info("productReturn: orderId={}, state={}",
                result == null ? null : result.getOrderId(),
                result == null ? null : result.getState());
        return result;
    }

    @Override
    public OrderDto payment(UUID orderId) {
        log.info("payment: orderId={}", orderId);

        OrderDto result = orderService.payment(orderId);

        log.info("payment: orderId={}, state={}",
                result == null ? null : result.getOrderId(),
                result == null ? null : result.getState());
        return result;
    }

    @Override
    public OrderDto paymentFailed(UUID orderId) {
        log.info("paymentFailed: orderId={}", orderId);

        OrderDto result = orderService.paymentFailed(orderId);

        log.info("paymentFailed: orderId={}, state={}",
                result == null ? null : result.getOrderId(),
                result == null ? null : result.getState());
        return result;
    }

    @Override
    public OrderDto delivery(UUID orderId) {
        log.info("delivery: orderId={}", orderId);

        OrderDto result = orderService.delivery(orderId);

        log.info("delivery: orderId={}, state={}",
                result == null ? null : result.getOrderId(),
                result == null ? null : result.getState());
        return result;
    }

    @Override
    public OrderDto deliveryFailed(UUID orderId) {
        log.info("deliveryFailed: orderId={}", orderId);

        OrderDto result = orderService.deliveryFailed(orderId);

        log.info("deliveryFailed: orderId={}, state={}",
                result == null ? null : result.getOrderId(),
                result == null ? null : result.getState());
        return result;
    }

    @Override
    public OrderDto complete(UUID orderId) {
        log.info("complete: orderId={}", orderId);

        OrderDto result = orderService.complete(orderId);

        log.info("complete: orderId={}, state={}",
                result == null ? null : result.getOrderId(),
                result == null ? null : result.getState());
        return result;
    }

    @Override
    public OrderDto calculateTotalCost(UUID orderId) {
        log.info("calculateTotalCost: orderId={}", orderId);

        OrderDto result = orderService.calculateTotalCost(orderId);

        log.info("calculateTotalCost: orderId={}, totalPrice={}",
                result == null ? null : result.getOrderId(),
                result == null ? null : result.getTotalPrice());
        return result;
    }

    @Override
    public OrderDto calculateDeliveryCost(UUID orderId) {
        log.info("calculateDeliveryCost: orderId={}", orderId);

        OrderDto result = orderService.calculateDeliveryCost(orderId);

        log.info("calculateDeliveryCost: orderId={}, deliveryPrice={}",
                result == null ? null : result.getOrderId(),
                result == null ? null : result.getDeliveryPrice());
        return result;
    }

    @Override
    public OrderDto assembly(UUID orderId) {
        log.info("assembly: orderId={}", orderId);

        OrderDto result = orderService.assembly(orderId);

        log.info("assembly: orderId={}, state={}",
                result == null ? null : result.getOrderId(),
                result == null ? null : result.getState());
        return result;
    }

    @Override
    public OrderDto assemblyFailed(UUID orderId) {
        log.info("assemblyFailed: orderId={}", orderId);

        OrderDto result = orderService.assemblyFailed(orderId);

        log.info("assemblyFailed: orderId={}, state={}",
                result == null ? null : result.getOrderId(),
                result == null ? null : result.getState());
        return result;
    }


}