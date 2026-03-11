package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.delivery.dto.DeliveryDto;
import ru.yandex.practicum.commerce.delivery.enums.DeliveryState;
import ru.yandex.practicum.commerce.order.dto.OrderDto;
import ru.yandex.practicum.commerce.warehouse.controller.WarehouseApi;
import ru.yandex.practicum.commerce.warehouse.dto.ShippedToDeliveryRequest;
import ru.yandex.practicum.controller.OrderFeignClient;
import ru.yandex.practicum.controller.WarehouseFeignClient;
import ru.yandex.practicum.error.DeliveryNotFoundException;
import ru.yandex.practicum.mapper.DeliveryMapper;
import ru.yandex.practicum.model.Delivery;
import ru.yandex.practicum.model.DeliveryAddress;
import ru.yandex.practicum.repository.DeliveryRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryMapper deliveryMapper;

    private final WarehouseFeignClient warehouseClient;
    private final OrderFeignClient orderClient;

    // PUT /api/v1/delivery
    @Transactional
    public DeliveryDto planDelivery(DeliveryDto request) {
        log.info("planDelivery: orderId={}", request.getOrderId());

        Delivery entity = deliveryMapper.toEntity(request);

        entity.setDeliveryId(null);

        entity.setDeliveryState(DeliveryState.CREATED);

        entity.setFragile(Boolean.TRUE.equals(request.getFragile()));
        entity.setDeliveryWeight(request.getDeliveryWeight() == null ? 0d : request.getDeliveryWeight());
        entity.setDeliveryVolume(request.getDeliveryVolume() == null ? 0d : request.getDeliveryVolume());

        Delivery saved = deliveryRepository.save(entity);
        DeliveryDto result = deliveryMapper.toDto(saved);

        log.info("planDelivery: deliveryId={}, orderId={}, state={}",
                result.getDeliveryId(), result.getOrderId(), result.getDeliveryState());
        return result;
    }

    @Transactional(readOnly = true)
    public BigDecimal deliveryCost(OrderDto request) {
        UUID orderId = request.getOrderId();
        log.info("deliveryCost: orderId={}", orderId);

        Delivery delivery = getByOrderId(orderId);

        BigDecimal cost = calculateCost(delivery);

        log.info("deliveryCost: orderId={}, cost={}", orderId, cost);
        return cost;
    }

    @Transactional
    public void deliveryPicked(UUID orderId) {
        log.info("ENTER deliveryPicked: orderId={}", orderId);

        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new DeliveryNotFoundException("Not Found orderId"));
        if (delivery.getDeliveryState() != DeliveryState.CREATED) {
            log.error("deliveryPicked: invalid state transition, orderId={}, currentState={}",
                    orderId, delivery.getDeliveryState());
            throw new IllegalStateException(
                    "Delivery cannot be moved to IN_PROGRESS from state " + delivery.getDeliveryState()
            );
        }
        delivery.setDeliveryState(DeliveryState.IN_PROGRESS);
        ShippedToDeliveryRequest request =
                new ShippedToDeliveryRequest(orderId, delivery.getDeliveryId());

        warehouseClient.shippedToDelivery(request);
        orderClient.assembly(orderId);
        log.info("EXIT deliveryPicked: orderId={}, deliveryId={}, state={}",
                orderId, delivery.getDeliveryId(), delivery.getDeliveryState());
    }

    @Transactional
    public void deliverySuccessful(UUID orderId) {
        log.info("deliverySuccessful: orderId={}", orderId);

        Delivery delivery = getByOrderId(orderId);
        delivery.setDeliveryState(DeliveryState.DELIVERED);
        deliveryRepository.save(delivery);
        orderClient.delivery(orderId);
    }

    @Transactional
    public void deliveryFailed(UUID orderId) {
        log.info("deliveryFailed: orderId={}", orderId);

        Delivery delivery = getByOrderId(orderId);
        delivery.setDeliveryState(DeliveryState.FAILED);
        deliveryRepository.save(delivery);
        orderClient.deliveryFailed(orderId);
    }


    private Delivery getByOrderId(UUID orderId) {
        return deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("NoDeliveryFound: orderId=" + orderId));
    }

    private BigDecimal calculateCost(Delivery d) {
        double base = 5.0;
        double sum = base;

        String fromStr = stringifyAddress(d.getFromAddress());
        if (fromStr.contains("ADDRESS_2")) {
            sum = sum + (base * 2);
        } else if (fromStr.contains("ADDRESS_1")) {
            sum = sum + (base * 1);
        } else {
            sum = sum + base;
        }

        if (d.isFragile()) {
            sum = sum + (sum * 0.2);
        }

        sum = sum + (d.getDeliveryWeight() * 0.3);
        sum = sum + (d.getDeliveryVolume() * 0.2);

        String fromStreet = d.getFromAddress() == null ? null : d.getFromAddress().getStreet();
        String toStreet = d.getToAddress() == null ? null : d.getToAddress().getStreet();

        boolean sameStreet = fromStreet != null && toStreet != null && fromStreet.equalsIgnoreCase(toStreet);
        if (!sameStreet) {
            sum = sum + (sum * 0.2);
        }

        return BigDecimal.valueOf(sum).setScale(2, RoundingMode.HALF_UP);
    }

    private String stringifyAddress(DeliveryAddress a) {
        if (a == null) return "";
        return (a.getCountry() == null ? "" : a.getCountry()) + " " +
                (a.getCity() == null ? "" : a.getCity()) + " " +
                (a.getStreet() == null ? "" : a.getStreet()) + " " +
                (a.getHouse() == null ? "" : a.getHouse()) + " " +
                (a.getFlat() == null ? "" : a.getFlat());
    }
}