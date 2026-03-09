package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.delivery.controller.DeliveryApi;
import ru.yandex.practicum.commerce.delivery.dto.DeliveryDto;
import ru.yandex.practicum.commerce.order.dto.OrderDto;
import ru.yandex.practicum.service.DeliveryService;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class DeliveryController implements DeliveryApi {

    private final DeliveryService deliveryService;

    @Override
    public DeliveryDto planDelivery(@Valid DeliveryDto request) {
        log.info("planDelivery: orderId={}, fromStreet={}, toStreet={}",
                request == null ? null : request.getOrderId(),
                request == null || request.getFromAddress() == null ? null : request.getFromAddress().getStreet(),
                request == null || request.getToAddress() == null ? null : request.getToAddress().getStreet());

        DeliveryDto result = deliveryService.planDelivery(request);

        log.info("planDelivery: deliveryId={}, orderId={}, state={}",
                result == null ? null : result.getDeliveryId(),
                result == null ? null : result.getOrderId(),
                result == null ? null : result.getDeliveryState());

        return result;
    }

    @Override
    public BigDecimal deliveryCost(OrderDto request) {
        log.info("deliveryCost: orderId={}", request == null ? null : request.getOrderId());

        BigDecimal cost = deliveryService.deliveryCost(request);

        log.info("deliveryCost: orderId={}, cost={}",
                request == null ? null : request.getOrderId(),
                cost);

        return cost;
    }

    @Override
    public void deliveryPicked(UUID orderId) {
        log.info("deliveryPicked: orderId={}", orderId);
        deliveryService.deliveryPicked(orderId);
        log.info("deliveryPicked: orderId={} -> OK", orderId);
    }

    @Override
    public void deliverySuccess(UUID orderId) {
        log.info("deliverySuccessful: orderId={}", orderId);
        deliveryService.deliverySuccessful(orderId);
        log.info("deliverySuccessful: orderId={} -> OK", orderId);
    }


    @Override
    public void deliveryFailed(UUID orderId) {
        log.info("deliveryFailed: orderId={}", orderId);
        deliveryService.deliveryFailed(orderId);
        log.info("deliveryFailed: orderId={} -> OK", orderId);
    }
}